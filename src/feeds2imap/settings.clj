(ns feeds2imap.settings
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.core.typed :refer [ann Any Set HMap U IFn]]
            [feeds2imap.gpg :refer [gpg]]
            [feeds2imap.logging :refer [info error]]
            [feeds2imap.types :refer :all]
            [feeds2imap.annotations :refer :all]
            [clojure.pprint :refer [pprint]])
  (:import  [java.io File]))

(ann default-config-dir [-> String])
(defn ^:private default-config-dir []
  (str (System/getenv "HOME") "/.config/feeds2imap.clj/"))

(ann config-dir [-> String])
(defn ^:private config-dir []
  (str (or (System/getenv "FEEDS2IMAP_HOME")
           (default-config-dir))))

(ann file [String -> File])
(defn ^File file [^String path]
  (File. path))

(ann bootstrap-config-dir [-> Any])
(defn ^:private bootstrap-config-dir []
  (let [file (file (config-dir))]
    (when-not (.exists file)
      (.mkdirs file))))

(ann bootstrap-file [String Any & :optional {:force Boolean} -> Any])
(defn ^:private bootstrap-file
  [path initial & {:keys [force] :or {force false}}]
  (let [file (file path)]
    (when (or force (not (.exists file)))
      (.createNewFile file)
      (spit path (str initial)))))

(ann ^:no-check read-or-create-file (IFn [String (Set String) -> Cache]
                                         [String (HMap) -> (Folder Urls)]
                                         [String (HMap) -> ImapConfiguration]
                                         [String String -> ImapConfiguration]))
(defn ^:private read-or-create-file [path initial]
  (let [path (str (config-dir) path)]
    (bootstrap-config-dir)
    (bootstrap-file path initial)
    (edn/read-string (slurp path))))

(ann read-encrypted-file [String -> (U ImapConfiguration Boolean)])
(defn ^:private read-encrypted-file [path]
  (bootstrap-config-dir)
  (let [path (str (config-dir) path)]
    (when (.exists (file path))
      (let [{:keys [out err exit]} (gpg "--quiet"
                                        "--batch"
                                        "--decrypt"
                                        "--"
                                        path)]
        (if (pos? exit)
          (do
            (error "Could not decrypt credentials from" path)
            (error err)
            (error "Make sure gpg is installed and works.")
            false)
          (edn/read-string out))))))

(ann write-file [String (U String Cache (Folder Urls)) -> Any])
(defn ^:private write-file [path data]
  (bootstrap-config-dir)
  (bootstrap-file (str (config-dir) path) data :force true))

(ann read-items [-> (Set String)])
(defn read-items []
  (read-or-create-file "read-items.clj" (hash-map)))

(ann ^:no-check clean-up-items [Cache -> Cache])
(defn clean-up-items [data]
  (info "Cleaning up cache of" (count data) "items.")
  (let [current-millis (System/currentTimeMillis)
                  ;  365 days
        threshold (* 365 24 60 60 1000)]
    (->> data
         (filter (fn [[checksum t]]
                   (< (- current-millis t)
                      threshold)))
         (into {}))))

(ann write-items [Cache -> Any])
(defn write-items [data]
  (let [clean-data (clean-up-items data)]
    (info "Writing" (count clean-data) "items to cache.")
    (write-file "read-items.clj" clean-data)))

(ann encrypted-imap [-> (U ImapConfiguration Boolean)])
(defn ^:private encrypted-imap []
  (read-encrypted-file "imap.clj.gpg"))

(ann unencrypted-imap [-> ImapConfiguration])
(defn ^:private unencrypted-imap []
  (read-or-create-file "imap.clj" (hash-map)))

(ann ^:no-check imap [-> ImapConfiguration])
(defn imap []
  (or (encrypted-imap)
      (unencrypted-imap)))

(ann ^:no-check handle-gpg-result [ShellResult -> Boolean])
(defn ^:private handle-gpg-result [{:keys [out err exit]}]
  (if (pos? exit)
    (do
      (error "Error executing gpg command:")
      (error "exit code is" exit)
      (error out)
      (error err)
      (error "Make sure gpg is installed and works.")
      false)
    (do
      (info out)
      true)))

(ann ^:no-check rm! [String -> Any])
(defn ^:private rm! [path]
  (let [file (File. path)]
    (when (.exists file)
      (.delete file))))

(ann encrypted-imap-path [-> String])
(defn ^:private encrypted-imap-path [] (str (config-dir) "imap.clj.gpg"))

(ann unencrypted-imap-path [-> String])
(defn ^:private unencrypted-imap-path [] (str (config-dir) "imap.clj"))

(ann encryption-recipient [-> String])
(defn ^:private encryption-recipient []
  (str (System/getenv "FEEDS_ENC_RECIPIENT")))

(ann ^:no-check encrypt-imap! [-> Any])
(defn encrypt-imap! []
  (let [result (gpg "--quiet"
                    "--batch"
                    "--recipient" (encryption-recipient)
                    "--encrypt"
                    "--output" (encrypted-imap-path)
                    "--"
                    (unencrypted-imap-path))]
    (when (handle-gpg-result result)
      (rm! (unencrypted-imap-path)))))

(ann ^:no-check decrypt-imap! [-> Any])
(defn decrypt-imap! []
  (let [result (gpg "--quiet"
                    "--batch"
                    "--decrypt"
                    "--output" (unencrypted-imap-path)
                    "--"
                    (encrypted-imap-path))]
    (when (handle-gpg-result result)
      (rm! (encrypted-imap-path)))))


(ann urls (IFn [-> (Folder Urls)]
               [(Folder Urls) -> Any]))
(defn urls
  ([] (read-or-create-file "urls.clj" (hash-map)))
  ([data] (write-file "urls.clj" (with-out-str (pprint data)))))
