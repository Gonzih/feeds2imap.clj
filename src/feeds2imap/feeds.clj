(ns feeds2imap.feeds
  (:require [hiccup.core :refer :all]
            [feedparser-clj.core :refer :all]
            [feeds2imap.message :as message]
            [clojure.data.codec.base64 :as b64]
            [clojure.pprint :refer :all]
            [feeds2imap.types]
            [feeds2imap.logging :refer [info error]]
            [feeds2imap.macro :refer :all]
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

(s/def ::new-items :feeds2imap.types/folder-of-items)

(s/def ::map-fn (s/fspec
                 :args (s/cat :item :feeds2imap.types/item)
                 :ret  :feeds2imap.types/item))

(s/def ::filter-fn (s/fspec
                    :args (s/cat :item :feeds2imap.types/item)
                    :ret :feeds2imap.types/boolean))

(s/fdef map-items
        :args (s/cat :fun ::map-fn
                     :items :feeds2imap.types/folder-of-items)
        :ret :feeds2imap.types/folder-of-items)

(defn map-items
  "Map function over items for each folder."
  [fun coll]
  (->> coll
       (map (fn [[folder items]] [folder (map fun items)]))
       (into {})))

(s/fdef pmap-items
        :args (s/cat :fun ::map-fn
                     :items :feeds2imap.types/folder-of-items)
        :ret :feeds2imap.types/folder-of-items)

(defn pmap-items
  "Map function over items for each folder using pmap."
  [fun coll]
  (->> coll
       (pmap (fn [[folder items]] [folder (pmap fun items)]))
       (into {})))

(s/fdef filter-items
        :args (s/cat :fun ::filter-fn
                     :items :feeds2imap.types/folder-of-items)
        :ret :feeds2imap.types/folder-of-items)

(defn filter-items
  "Filter items for each folder.
   Filter folders with non empty items collection."
  [fun coll]
  (->> coll
       (map (fn [[folder items]]
              [folder (filter fun items)]))
       (filter (fn [[folder items]]
                 (seq items)))
       (into {})))

(s/fdef flatten-items
        :args (s/cat :items :feeds2imap.types/folder-of-unflattened-items)
        :ret :feeds2imap.types/folder-of-items)

(defn flatten-items [items]
  (->> items
       (map (fn [[folder items]] [folder (flatten items)]))
       (into {})))

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
        :args (s/cat :cache :feeds2imap.types/cache :item :feeds2imap.types/item)
        :ret :feeds2imap.types/boolean)

(defn new?
  "Looks up item in the cache"
  [cache item]
  (not (contains? cache (md5-identifier item))))

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

(s/fdef to-email-map
        :args (s/cat :from :feeds2imap.types/string :to :feeds2imap.types/string :item :feeds2imap.types/item)
        :ret :feeds2imap.types/message)

(defn to-email-map
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

(defn items->emails [session from to item]
  (message/from-map session (to-email-map from to item)))

(s/fdef items->emails
        :args (s/cat :session :feeds2imap.types/mail-session
                     :from :feeds2imap.types/string
                     :to :feeds2imap.types/string
                     :items (s/map-of :feeds2imap.types/keyword :feeds2imap.types/item))
        :ret :feeds2imap.types/mime-message)

(defn to-emails
  "Convert items to Messages."
  [session from to items]
  (map-items (partial items->emails session from to) items))

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
        :args (s/cat :url :feeds2imap.types/url)
        :ret :feeds2imap.types/parsed-feed)

(defn parse [url]
  (letfn [(log-try [url n-try reason]
            (if (> n-try 1)
              (error "Fetching" url "try" n-try "reason is" reason)
              (info  "Fetching" url)))
          (parse-try
            ([url] (parse-try url 1 :no-reason))
            ([url n-try reason]
             (log-try url n-try reason)
             (try*
              (if (< n-try 3)
                (fetch url)
                {:entries ()})
              (catch* [ConnectException
                       NoRouteToHostException
                       UnknownHostException
                       ParsingFeedException
                       IllegalArgumentException
                       IOException] e (parse-try url (inc n-try) e)))))]
    (parse-try url)))

(s/fdef reduce-new-items
        :args (s/cat :cache :feeds2imap.types/cache
                     :parsed-feeds :feeds2imap.types/folder-of-items)
        :ret (s/keys :req-un [::new-items :feeds2imap.types/cache]))

(defn reduce-new-items [cache parsed-feeds]
  (reduce (fn [accumulator [folder items]]
            (reduce (fn [{cache-inner :cache :as accumulator-inner} item]
                      (if (new? cache-inner item)
                        (let [md5-hash (md5-identifier item)
                              ts (System/currentTimeMillis)]
                          (-> accumulator-inner
                              (update-in [:cache]            (fn [new-cache] (assoc new-cache md5-hash ts)))
                              (update-in [:new-items folder] (fn [new-items] (conj new-items item)))))
                        accumulator-inner))
                    accumulator
                    items))
          {:cache cache :new-items {}}
          parsed-feeds))

(s/fdef new-items
        :args (s/cat :cache :feeds2imap.types/cache
                     :urls :feeds2imap.types/folder-of-urls)
        :ret (s/keys :req-un [::new-items :feeds2imap.types/cache]))

(defn new-items [cache urls]
  (->> urls
       (pmap-items parse)
       (map-items :entries)
       flatten-items
       (reduce-new-items cache)))
