(ns feeds2imap.opml
  (:require [clojure.pprint :refer [pprint]]
            [clojure.spec :as s]
            [clojure.xml]
            [clojure.java.io :refer [file writer]])
  (:import  [java.io File]))

(defn opml-find-tag
  [tag-keyword xml]
  (when xml
    (if (sequential? xml)
      (->> xml
           (map #(opml-find-tag tag-keyword %))
           (filter identity)
           first)
      (if (= tag-keyword (:tag xml))
        xml
        (recur tag-keyword (:content xml))))))

(defn opml-folder-keyword
  [xml-el]
  (->> (:title (:attrs xml-el))
       (replace {\space \-})
       (filter #(or (java.lang.Character/isLetterOrDigit ^java.lang.Character %)
                    (#{\- \_ \. \? \! \# \$ \%} %)))
       (apply str)
       keyword))

(defn opml-build-entry
  "Returns a string with a feed URL or a map."
  [xml-outline]
  ;; nested folders not supported
  (:xmlUrl (:attrs xml-outline)))

(defn opml-build-map
  "Returns a map of :foldernames to vectors of URLs.
  Entries without a folder are assigned to :__global__."
  [xml-outlines]
  (let [update-m (fn [m xml-el]
                   (if (not (:xmlUrl (:attrs xml-el)))
                     ;; a folder
                     (let [key   (opml-folder-keyword xml-el)
                           inner (->> (:content xml-el)
                                      (map opml-build-entry)
                                      (filter identity)
                                      vec)]
                       (assoc m key inner))
                     ;; an entry
                     (update-in m [:__global__] conj (:xmlUrl xml-el))))]
    (reduce update-m {} xml-outlines)))

(defn convert-opml [istream]
  "Takes an input stream with OPML contents, returns a map for `urls.clj`."
  (let [x (clojure.xml/parse istream)
        b (opml-find-tag :body x)
        m (opml-build-map (:content b))]
    m))

(defn convert-and-print-from-file! [path]
  (->> (File. ^String path)
       convert-opml
       pprint))

(defn convert-and-write-to-file! [from to]
  (let [sink (writer (file to))
        data (convert-opml (file from))]
    (pprint data sink)))
