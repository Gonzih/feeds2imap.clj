(ns feeds2imap.feeds
  (:require [hiccup.core :refer :all]
            [feedparser-clj.core :refer :all]
            [feeds2imap.message :as message]
            [clojure.data.codec.base64 :as b64]
            [clojure.pprint :refer :all]
            [feeds2imap.types]
            [feeds2imap.logging :refer [info error]]
            [feeds2imap.macro :refer :all]
            [feeds2imap.db :refer [is-new?]]
            [digest :refer [md5]]
            [clojure.string :as string]
            [clojure.spec.alpha :as s])
  (:import  [java.lang IllegalArgumentException]
            [java.security MessageDigest]
            [java.net NoRouteToHostException ConnectException UnknownHostException HttpURLConnection URL]
            [java.io IOException]
            [javax.mail.internet MimeMessage]
            [com.sun.syndication.io ParsingFeedException]
            [java.util Date]))

(s/fdef item-authors
        :args (s/cat :item :feeds2imap.types/item)
        :ret :feeds2imap.types/string)

(defn item-authors
  "Format each author as
   \"Name <name[at]example.com> http://example.com/\".
   Multiple authors are coma-separated"
  [{:keys [author authors] :as item}]
  (letfn [(format-author [author]
            (let [{:keys [name email uri]} author
                  email (when email
                          (format "<%s>" (apply str (replace {\@ "[at]"} email))))
                  fields (filter (complement nil?)
                                 [name email uri])]
              (string/join " " fields)))]
    (if (seq authors)
      (string/join ", " (map format-author authors))
      (str author))))

(s/fdef uniq-identifier
        :args (s/cat :item :feeds2imap.types/item)
        :ret :feeds2imap.types/string)

(defn uniq-identifier
  "Generates unique identifier for item.
   First try uri, then url, then link.
   Only if items listed above are empty use md5 of title + link + authors."
  [{:keys [title uri url link] :as item}]
  (string/replace (or uri url link (str title link (item-authors item)))
                  #"http://" "https://"))

(s/fdef md5-identifier
        :args (s/cat :item :feeds2imap.types/item)
        :ret :feeds2imap.types/string)

(defn md5-identifier [item]
  (-> item uniq-identifier md5))

(s/fdef new?
        :args (s/cat :item :feeds2imap.types/item)
        :ret :feeds2imap.types/boolean)

(defn new?
  "Looks up item in the cache"
  [item]
  (is-new? (md5-identifier item)))

(s/fdef item-content
        :args (s/cat :item :feeds2imap.types/item)
        :ret :feeds2imap.types/string)

(defn item-content [item]
  (str (or (-> item :contents first :value)
           (-> item :description :value))))

(s/fdef encoded-word
        :args (s/cat :s :feeds2imap.types/string)
        :ret :feeds2imap.types/string)

(defn ^:private encoded-word
  "Encodes From: field. See http://en.wikipedia.org/wiki/MIME#Encoded-Word"
  [s]
  (let [encoded-text (String. (b64/encode (.getBytes s "UTF-8")))]
    (str "=?UTF-8?B?" encoded-text "?=")))

(s/fdef item-pubdate
        :args (s/cat :item :feeds2imap.types/item)
        :ret (s/nilable :feeds2imap.types/date))

(defn item-pubdate [item]
  (or (:updated-date item) (:published-date item)))

(s/fdef escape-title
        :args (s/cat :s :feeds2imap.types/string)
        :ret :feeds2imap.types/string)

(defn escape-title [title]
  (string/replace title #"/\r?\n|\r/" " "))

(s/fdef ->email-map
        :args (s/cat :from :feeds2imap.types/string :to :feeds2imap.types/string :item :feeds2imap.types/item)
        :ret :feeds2imap.types/message)

(defn ->email-map
  "Convert item to map for Message construction."
  [from to item]
  (let [{:keys [title link]} item
        authors (item-authors item)
        content (item-content item)
        from+   (string/join " " [(encoded-word authors) (str "<" from ">")])
        pubdate (item-pubdate item)
        html (html [:table
                    [:tbody [:tr [:td [:a {:href link} title] [:hr]]]
                     (when (seq authors)
                       [:tr [:td authors [:hr]]])
                     [:tr [:td content]]]])]
    {:from from+ :to to :date pubdate :subject title :html html}))

(s/fdef items->emails
        :args (s/cat :session :feeds2imap.types/mail-session
                     :from :feeds2imap.types/string
                     :to :feeds2imap.types/string
                     :item :feeds2imap.types/item)
        :ret :feeds2imap.types/mime-message)

(defn item->email [session from to item]
  (message/from-map session (->email-map from to item)))


(defn ->emails
  "Convert items to Messages."
  [session from to items]
  (map (fn [{:keys [folder] :as item}]
         {:mime-message (item->email session from to item)
          :folder folder})
       items))

(s/fdef set-entries-authors
        :args (s/cat :feed :feeds2imap.types/parsed-feed)
        :ret :feeds2imap.types/parsed-feed)

(defn set-entries-authors [feed]
  (let [feed-as-author {:name (:title feed) :uri (:link feed)}
        set-authors (fn [e]
                      (if (seq (:authors e))
                        e
                        (assoc e :authors [feed-as-author])))
        entries (map set-authors (:entries feed))]
    (assoc feed :entries entries)))

(s/fdef fetch
        :args (s/cat :url :feeds2imap.types/url)
        :ret :feeds2imap.types/parsed-feed)

(defn fetch [url]
  (-> (doto (cast HttpURLConnection (-> url URL. .openConnection))
        (.setRequestProperty  "User-Agent" "feeds2imap.clj/0.3 (+https://github.com/Gonzih/feeds2imap.clj)"))
      parse-feed
      set-entries-authors))

(s/fdef parse
        :args (s/cat :folder :feeds2imap.types/folder-and-url)
        :ret :feeds2imap.types/parsed-feed)

(defn parse [{:keys [url folder]}]
  (info  "Fetching" url)
  (let [response (try*
                   (fetch url)
                   (catch* [ConnectException
                            NoRouteToHostException
                            UnknownHostException
                            ParsingFeedException
                            IllegalArgumentException
                            IOException] e {:entries ()}))]
    (update
      response
      :entries
      (partial map #(assoc % :folder folder)))))

(s/fdef filter-new-items
        :args (s/cat :parsed-feeds :feeds2imap.types/items)
        :ret :feeds2imap.types/items)

(defn filter-new-items [parsed-feeds]
  (->> parsed-feeds
       (filter new?)))

(s/fdef flatten-urls
        :args (s/cat :urls :feeds2imap.types/folder-of-urls)
        :ret :feeds2imap.types/folder-and-url-coll)

(defn flatten-urls [urls]
  (->> urls
       (map (fn [[folder urls]] (map (fn [url] {:folder folder :url url}) urls)))
       flatten))

(s/fdef new-items
        :args (s/cat :urls :feeds2imap.types/folder-of-urls)
        :ret :feeds2imap.types/items)

(defn new-items [urls]
  (->> urls
       flatten-urls
       (map parse)
       :entries
       flatten
       filter-new-items))

(s/fdef extract-guids
        :args (s/cat :items :feeds2imap.types/items)
        :ret (s/coll-of :feeds2imap.types/string))

(defn extract-guids [items]
  (map md5-identifier items))
