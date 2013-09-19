(ns feeds2imap.settings
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.logging :refer [info error]]
            [clojure.core.typed :refer :all]
            [feeds2imap.types :refer :all]
            [feeds2imap.annotations :refer :all])
  (:import  [java.io File]
            [clojure.lang Keyword]))

(ann config-dir [-> String])
(defn ^:private config-dir []
  (str (System/getenv "HOME") "/.config/feeds2imap.clj/"))

(ann file [String -> File])
(defn ^File file [^String path]
  (File. path))

(ann bootstrap-config-dir [-> Any])
(defn ^:private bootstrap-config-dir []
  (let [file (file (config-dir))]
    (when-not (.exists file)
      (.mkdirs file))))

(ann bootstrap-file [String Any & :optional {:force Boolean} -> Any])
(defn ^:private bootstrap-file [path initial & {:keys [force] :or {force false}}]
  (let [file (file path)]
    (when (or force (not (.exists file)))
      (.createNewFile file)
      (spit path (str initial)))))

(ann ^:no-check read-or-create-file (Fn [String (Set String) -> Cache]
                                        [String (HMap) -> (Folder Urls)]))
(defn ^:private read-or-create-file [path initial]
  (let [path (str (config-dir) path)]
    (bootstrap-config-dir)
    (bootstrap-file path initial)
    (edn/read-string (slurp path))))

(ann write-file [String (U Cache (Folder Urls)) -> Any])
(defn ^:private write-file [path data]
  (bootstrap-config-dir)
  (bootstrap-file (str (config-dir) path) data :force true))

(ann read-items [-> (Set String)])
(defn read-items []
  (read-or-create-file "read-items.clj" (hash-set)))

(ann write-items [Cache -> Any])
(defn write-items [data]
  (info "Writing" (count data) "items to cache.")
  (write-file "read-items.clj" data))

(ann imap [-> (Folder Urls)])
(defn imap []
  (read-or-create-file "imap.clj" (hash-map)))

(ann urls (Fn [-> (Folder Urls)]
              [(Folder Urls) -> Any]))
(defn urls
  ([] (read-or-create-file "urls.clj" (hash-map)))
  ([data] (write-file "urls.clj" data)))
