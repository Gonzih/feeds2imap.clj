(ns feeds2imap.opml
  (:require [clojure.pprint :refer [pprint]]
            [clojure.core.typed :refer [ann Keyword Map Vec]]
            [feeds2imap.types :refer :all]
            [feeds2imap.annotations :refer :all]
            [clojure.xml])
  (:import  [java.io File]))

(ann ^:no-check opml-find-tag [Keyword XML -> Keyword])
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

(ann ^:no-check opml-folder-keyword [XML -> Keyword])
(defn opml-folder-keyword
  [xml-el]
  (->> (:title (:attrs xml-el))
       (replace {\space \-})
       (filter #(or (java.lang.Character/isLetterOrDigit ^java.lang.Character %)
                    (#{\- \_ \. \? \! \# \$ \%} %)))
       (apply str)
       keyword))

(ann ^:no-check opml-build-entry [XML -> String])
(defn opml-build-entry
  "Returns a string with a feed URL or a map."
  [xml-outline]
  ;; nested folders not supported
  (:xmlUrl (:attrs xml-outline)))

(ann ^:no-check opml-build-map [XML -> (Map Keyword (Vec String))])
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

(ann ^:no-check convert-opml [File -> (Folder Urls)])
(defn convert-opml [istream]
  "Takes an input stream with OPML contents, returns a map for `urls.clj`."
  (let [x (clojure.xml/parse istream)
        b (opml-find-tag :body x)
        m (opml-build-map (:content b))]
    m))

(ann ^:no-check convert-and-print-from-file! [File -> Any])
(defn convert-and-print-from-file! [path]
  (->> (File. ^String path)
       convert-opml
       pprint))

(ann ^:no-check convert-and-print-from-file! [File File -> Any])
(defn convert-and-write-to-file! [from to]
  (let [sink (writer (file to))
        data (convert-opml (File. ^String from))]
    (pprint data sink)))
