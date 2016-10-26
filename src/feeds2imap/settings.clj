(ns feeds2imap.settings
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.spec :as s]
            [clojure.spec.test :as stest]
            [feeds2imap.types]
            [feeds2imap.gpg :refer [gpg]]
            [feeds2imap.logging :refer [info error]]
            [clojure.pprint :refer [pprint]])
  (:import  [java.io File]
            [java.lang RuntimeException]))

(s/def ::file (partial instance? File))

(s/fdef default-config-dir
        :args (s/cat)
        :ret :feeds2imap.types/string)

(defn ^:private default-config-dir []
  (str (System/getenv "HOME") "/.config/feeds2imap.clj/"))

(s/fdef config-dir
        :args (s/cat)
        :ret :feeds2imap.types/string)

(defn ^:private config-dir []
  (str (or (System/getenv "FEEDS2IMAP_HOME")
           (default-config-dir))))

(s/fdef file
        :args (s/cat :path :feeds2imap.types/string)
        :ret ::file)

(defn ^File file [^String path]
  (File. path))

(defn ^:private bootstrap-config-dir []
  (let [file (file (config-dir))]
    (when-not (.exists file)
      (.mkdirs file))))

(defn ^:private bootstrap-file
  [path initial & {:keys [force] :or {force false}}]
  (let [file (file path)]
    (when (or force (not (.exists file)))
      (.createNewFile file)
      (spit path (str initial)))))

(defn ^:private read-or-create-file [path initial]
  (let [path (str (config-dir) path)]
    (bootstrap-config-dir)
    (bootstrap-file path initial)
    (try
      (edn/read-string (slurp path))
      (catch RuntimeException e
        (error (str "RuntimeException while reading " path ": " (.getMessage e)))
        (throw e)))))

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

(defn ^:private write-file [path data]
  (bootstrap-config-dir)
  (bootstrap-file (str (config-dir) path) data :force true))

(s/fdef read-items
        :args (s/cat)
        :ret :feeds2imap.types/cache)

(defn read-items []
  {:post [(s/valid? :feeds2imap.types/cache %)]}
  (read-or-create-file "read-items.clj" (hash-map)))

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

(s/fdef write-items
        :args (s/cat :data :feeds2imap.types/cache))

(defn write-items [data]
  {:pre [(s/valid? :feeds2imap.types/cache data)]}
  (let [clean-data (clean-up-items data)]
    (info "Writing" (count clean-data) "items to cache.")
    (write-file "read-items.clj" clean-data)))

(s/fdef encrypted-imap
        :args (s/cat)
        :ret (s/nilable :feeds2imap.types/imap-configuration))

(defn ^:private encrypted-imap []
  (read-encrypted-file "imap.clj.gpg"))

(s/fdef unencrypted-imap
        :args (s/cat)
        :ret (s/nilable :feeds2imap.types/imap-configuration))

(defn ^:private unencrypted-imap []
  (read-or-create-file "imap.clj" (hash-map)))

(s/fdef imap
        :args (s/cat)
        :ret :feeds2imap.types/imap-configuration)

(defn imap []
  {:post [(or (s/valid? :feeds2imap.types/imap-configuration %)
              (s/explain :feeds2imap.types/imap-configuration %))]}
  (or (encrypted-imap)
      (unencrypted-imap)))

(s/fdef handle-gpg-result
        :args (s/cat :result :feeds2imap.types/shell-result)
        :ret :feeds2imap.types/boolean)

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

(defn ^:private rm! [path]
  (let [file (File. path)]
    (when (.exists file)
      (.delete file))))

(s/fdef encrypted-imap-path
        :args (s/cat)
        :ret :feeds2imap.types/string)

(defn ^:private encrypted-imap-path [] (str (config-dir) "imap.clj.gpg"))

(s/fdef unencrypted-imap-path
        :args (s/cat)
        :ret :feeds2imap.types/string)

(defn ^:private unencrypted-imap-path [] (str (config-dir) "imap.clj"))

(s/fdef encryption-recipient
        :args (s/cat)
        :ret :feeds2imap.types/string)

(defn ^:private encryption-recipient []
  (str (System/getenv "FEEDS_ENC_RECIPIENT")))

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

(defn decrypt-imap! []
  (let [result (gpg "--quiet"
                    "--batch"
                    "--decrypt"
                    "--output" (unencrypted-imap-path)
                    "--"
                    (encrypted-imap-path))]
    (when (handle-gpg-result result)
      (rm! (encrypted-imap-path)))))

(s/fdef urls
        :args (s/? :feeds2imap.types/folder-of-urls)
        :ret :feeds2imap.types/folder-of-urls)

(defn urls
  ([]
   {:post [(s/valid? :feeds2imap.types/folder-of-urls %)]}
   (read-or-create-file "urls.clj" (hash-map)))

  ([data]
   {:pre  [(or (s/valid? :feeds2imap.types/folder-of-urls data)
               (s/valid? map? data))]}
   (write-file "urls.clj" (with-out-str (pprint data)))))
