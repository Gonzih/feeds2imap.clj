(ns feeds2imap.feeds
  (:require [hiccup.core :refer :all]
            [feedparser-clj.core :refer :all]
            [feeds2imap.message :as message]
            [clojure.data.codec.base64 :as b64]
            [clojure.string :as s]
            [clojure.pprint :refer :all]
            [clojure.tools.logging :refer [info error]]
            [feeds2imap.macro :refer :all]
            [clojure.core.typed :refer :all]
            [feeds2imap.types :refer :all]
            [feeds2imap.annotations :refer :all]
            [digest :refer [md5]]
            [clojure.string :as s])
  (:import  [java.lang IllegalArgumentException]
            [java.security MessageDigest]
            [java.net NoRouteToHostException ConnectException UnknownHostException]
            [java.io IOException]
            [javax.mail Session]
            [javax.mail.internet MimeMessage]
            [clojure.lang Keyword]
            [com.sun.syndication.io ParsingFeedException]
            [java.util Date]))

(ann ^:no-check map-items (Fn [(Fn [ParsedFeed -> Items]) (Folder ParsedFeed) -> (Folder UnflattenedItems)]
                              [(Fn [Item -> Message]) (Folder Items) -> (Folder Messages)]))
(defn map-items
  "Map function over items for each folder."
  [fun coll]
  (map (fn [[folder items]] [folder (map fun items)]) coll))

(ann ^:no-check pmap-items [(Fn [String -> ParsedFeed]) (Folder Urls) -> (Folder ParsedFeed)])
(defn pmap-items
  "Map function over items for each folder using pmap."
  [fun coll]
  (pmap (fn [[folder items]] [folder (pmap fun items)]) coll))

(ann ^:no-check filter-items [(Fn [Item -> Boolean]) (Folder Items) -> (Folder Items)])
(defn filter-items
  "Filter items for each folder.
   Filter folders with non empty items collection."
  [fun coll]
  (->> coll
       (map (fn [[folder items]]
              [folder (filter fun items)]))
       (filter (fn [[folder items]]
                 (seq items)))))

(ann ^:no-check flatten-items [(Folder UnflattenedItems) -> (Folder Items)])
(defn flatten-items [items]
  (map (fn [[folder items]]
        [folder (flatten items)])
       items))

(ann ^:no-check item-authors [Item -> String])
(defn ^:private item-authors [{:keys [authors]}]
  "Format each author as
   \"Name <name[at]example.com> http://example.com/\".
   Multiple authors are coma-separated"
  (letfn [(format-author [author]
            (let [{:keys [name email uri]} author
                  email (when email
                          (format "<%s>" (apply str (replace {\@ "[at]"} email))))
                  fields (filter (complement nil?)
                                 [name email uri])]
              (s/join " " fields)))]
    (s/join ", " (map format-author authors))))

(ann ^:no-check uniq-identifier [Item -> String])
(defn uniq-identifier
  "Generates unique identifier for item.
   First try uri, then url, then link.
   Only if items listed above are empty use md5 of title + link + authors."
  [{:keys [title uri url link] :as item}]
  (s/replace (or uri url link (str title link (item-authors item)))
             #"http://" "https://"))

(ann md5-identifier [Item -> String])
(defn md5-identifier [item]
  (-> item uniq-identifier md5))

(ann new? [Cache Item -> Boolean])
(defn new?
  "Looks up item in the cache"
  [cache item]
  (not (contains? cache (md5-identifier item))))

(ann item-content [Item -> String])
(defn item-content [item]
  (str (or (-> item :contents first :value)
           (-> item :description :value))))

(ann ^:no-check encoded-word [String -> String])
(defn ^:private encoded-word
  "Encodes From: field. See http://en.wikipedia.org/wiki/MIME#Encoded-Word"
  [s]
  (let [encoded-text (String. (b64/encode (.getBytes s "UTF-8")))]
    (str "=?UTF-8?B?" encoded-text "?=")))

(ann ^:no-check item-pubdate [Item -> Date])
(defn item-pubdate [item]
  (or (:updated-date item) (:published-date item)))

(ann to-email-map [String String Item -> MessageMap])
(defn to-email-map
  "Convert item to map for Message construction."
  [from to item]
  (let [{:keys [title link]} item
        authors (item-authors item)
        content (item-content item)
        from+   (s/join " " [(encoded-word authors) (str "<" from ">")])
        pubdate (item-pubdate item)
        html (html [:table
                     [:tbody [:tr [:td [:a {:href link} title] [:hr]]]
                             (when (seq authors)
                               [:tr [:td authors [:hr]]])
                             [:tr [:td content]]]])]
    {:from from+ :to to :date pubdate :subject title :html html}))

(ann items-to-emails [Session String String Item -> Message])
(defn items-to-emails [session from to item]
  (message/from-map session (to-email-map from to item)))

(ann to-emails [Session String String (Folder Items) -> (Folder Messages)])
(defn to-emails
  "Convert items to Messages."
  [session from to items]
  (map-items (partial items-to-emails session from to) items))

(ann ^:no-check set-entries-authors [ParsedFeed -> ParsedFeed])
(defn set-entries-authors [feed]
  (let [feed-as-author {:name (:title feed) :uri (:link feed)}
        set-authors (fn [e]
                      (if (seq (:authors e))
                        e
                        (assoc e :authors [feed-as-author])))
        entries (map set-authors (:entries feed))]
    (assoc feed :entries entries)))

(ann ^:no-check parse [String -> ParsedFeed])
(defn parse [url]
  (letfn [(log-try [url n-try reason]
            (if (> n-try 1)
              (info "Fetching" url "try" n-try "reason is" reason)
              (info "Fetching" url)))
          (parse-try
            ([url] (parse-try url 1 :no-reason))
            ([url n-try reason]
              (log-try url n-try reason)
              (try*
                (if (< n-try 3)
                  (-> url parse-feed set-entries-authors)
                  {:entries ()})
                (catch* [ConnectException
                         NoRouteToHostException
                         UnknownHostException
                         ParsingFeedException
                         IllegalArgumentException
                         IOException] e (parse-try url (inc n-try) e)))))]
    (parse-try url)))

(ann ^:no-check reduce-new-items [Cache (Folder Items) -> (HMap :mandatory {:new-items (Folder Items)
                                                                            :cache Cache})])
(defn reduce-new-items [cache parsed-feeds]
  (reduce (fn [{:keys [cache new-items] :as val} [folder items]]
            (reduce (fn [{:keys [cache] :as val} item]
                      (if (new? cache item)
                        (-> val
                            (update-in [:cache] (fn [new-cache] (conj new-cache (md5-identifier item))))
                            (update-in [:new-items folder] (fn [new-items] (conj new-items item))))
                        val))
                    val
                    items))
          {:cache cache :new-items {}}
          parsed-feeds))

(ann new-items [Cache (Folder Urls) -> (HMap :mandatory {:new-items (Folder Items)
                                                         :cache Cache})])
(defn new-items [cache urls]
  (->> urls
       (pmap-items parse)
       (map-items :entries)
       flatten-items
       (reduce-new-items cache)))
