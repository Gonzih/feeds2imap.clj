(ns feeds2imap.settings
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.logging :refer [info error]])
  (:import  [java.io File]))

(defn ^:private config-dir []
  (str (System/getenv "HOME") "/.config/feeds2imap.clj/"))

(defn ^:private bootstrap-config-dir []
  (let [file (File. (config-dir))]
    (when-not (.exists file)
      (.mkdirs file))))

(defn ^:private bootstrap-file [path initial & {:keys [force] :or {force false}}]
  (let [file (File. path)]
    (when (or force (not (.exists file)))
      (.createNewFile file)
      (spit path (str initial)))))

(defn ^:private read-or-create-file [path initial]
  (let [path (str (config-dir) path)]
    (bootstrap-config-dir)
    (bootstrap-file path initial)
    (edn/read-string (slurp path))))

(defn ^:private write-file [path data]
  (bootstrap-config-dir)
  (bootstrap-file (str (config-dir) path) data :force true))

(defn read-items []
  (read-or-create-file "read-items.clj" (hash-set)))

(defn write-items [data]
  (info "Writing" (count data) "items to cache.")
  (write-file "read-items.clj" data))

(defn imap []
  (read-or-create-file "imap.clj" (hash-map)))

(defn urls []
  (read-or-create-file "urls.clj" (hash-map)))
