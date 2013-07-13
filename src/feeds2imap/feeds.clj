(ns feeds2imap.feeds
  (:require [hiccup.core :refer :all]
            [feedparser-clj.core :refer :all]
            [feeds2imap.message :as message]
            [clojure.string :as s]
            [clojure.pprint :refer :all]
            [clojure.tools.logging :refer [info error]]
            [feeds2imap.macro :refer :all])
  (:import  [java.security MessageDigest]
            [java.net NoRouteToHostException ConnectException UnknownHostException]
            [java.io IOException]))

(defn ^:private map-items
  "Map function over items for each folder."
  [fun coll]
  (map (fn [[folder items]] [folder (map fun items)]) coll))

(defn ^:private pmap-items
  "Map function over items for each folder using pmap."
  [fun coll]
  (pmap (fn [[folder items]] [folder (pmap fun items)]) coll))

(defn ^:private filter-items
  "Filter items for each folder.
   Filter folders with non empty items collection."
  [fun coll]
  (->> coll
       (map (fn [[folder items]]
              [folder (filter fun items)]))
       (filter (fn [[folder items]]
                 (seq items)))))

(defn ^:private flattern-items [items]
  (map (fn [[folder emails]]
        [folder (flatten emails)])
       items))

(defn ^:private item-authors [{:keys [authors]}]
  (s/join (map (fn [author]
                 (str (:name author)
                      " | "
                      (:email  author)
                      " | "
                      (:uri   author))) authors)))

(defn md5 [string]
  {:pre [(string? string)]}
  (let [md (MessageDigest/getInstance "MD5")]
    (.toString  (BigInteger. 1 (.digest md  (.getBytes string "UTF-8"))) 16)))

(defn ^:private digest
  "Generates unique digest for item."
  [{:keys [title link] :as item}]
  (md5 (str title link (item-authors item))))

(defn ^:private new?
  "Looks up item in the cache"
  [cache item]
  (not (contains? cache (digest item))))

(defn mark-all-as-read
  "Adds all items to cache.
   Returns updated cache."
  [cache items]
  (->> items
       (map last)
       flatten
       (map digest)
       (into cache)))

;TODO use hiccup here, extract all data from item
(defn to-email-map
  "Convert item to map for Message construction."
  [from to item]
  (let [{:keys [title link]} item
        authors (item-authors item)
        content (-> item :contents first :value)
        html (html [:table
                     [:tbody [:tr [:td [:a {:href link} title] [:hr]]]
                             (when (seq authors)
                               [:tr [:td authors [:hr]]])
                             [:tr [:td content]]]])]
    {:from from :to to :subject title :html html}))

(defn items-to-emails [session from to item]
  (message/from-map session (to-email-map from to item)))

(defn to-emails
  "Convert items to Messages."
  [session from to items]
  (map-items (partial items-to-emails session from to) items))

(defn parse [url]
  (letfn [(log-try [url n-try reason]
            (if (> n-try 1)
              (info "Fetching " url "try #" n-try "reason is" reason)
              (info "Fetching " url)))
          (parse-try
            ([url] (parse-try url 1 :no-reason))
            ([url n-try reason]
              (log-try url n-try reason)
              (try*
                (if (< n-try 10)
                  (parse-feed url)
                  {:entries ()})
                (catch* [ConnectException
                         NoRouteToHostException
                         UnknownHostException
                         IOException] e (parse-try url (inc n-try) e)))))]
    (parse-try url)))

(defn new-items [cache urls]
  (->> urls
       (pmap-items parse)
       (map-items :entries)
       (flattern-items)
       (filter-items (partial new? cache))))
