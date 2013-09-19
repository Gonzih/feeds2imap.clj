(ns feeds2imap.folder
  (:require [clojure.tools.logging :refer [info error]]
            [clojure.core.typed :refer :all]
            [feeds2imap.annotations :refer :all])
  (:import  [javax.mail Store Folder Message]
            [javax.mail.internet MimeMessage]
            [clojure.lang Keyword]))

(non-nil-return javax.mail.Store/getFolder :all)
(non-nil-return javax.mail.Folder/exists :all)
(non-nil-return javax.mail.Folder/create :all)

(ann get-folder [Store String -> Folder])
(defn ^Folder get-folder [^Store store ^String folder]
  (.getFolder store folder))

(ann exists [Store String -> Boolean])
(defn exists [^Store store folder]
  (.exists (get-folder store folder)))

(ann create [Store String -> (U Boolean nil)])
(defn create [^Store store folder]
  (when-not (exists store folder)
    (info "Creating IMAP folder" folder)
    (.create (get-folder store folder) Folder/HOLDS_MESSAGES)))

(ann ^:no-check append [Store String (Seqable MimeMessage) -> nil])
(defn append [store folder messages]
  (.appendMessages (get-folder store folder)
                   (into-array Message messages)))

(ann ^:no-check append-emails [Store (Map Keyword (Seqable MimeMessage)) -> Any])
(defn append-emails [store emails]
  (doall
    (pmap (fn [[folder emails]]
            (let [folder-str (str "RSS/" (name folder))]
              (create store folder-str)
              (info "Appending" (count emails) "emails in to the IMAP folder" folder-str)
              (append store folder-str emails)))
          emails)))
