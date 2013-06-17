(ns feeds2imap.folder
  (:require [clojure.tools.logging :refer [info error]])
  (:import  [javax.mail Folder Message]))

(defn get-folder [store folder]
  (.getFolder store folder))

(defn exists [store folder]
  (.exists (get-folder store folder)))

(defn create [store folder]
  (when-not (exists store folder)
    (.create (get-folder store folder) Folder/HOLDS_MESSAGES)))

(defn append [store folder messages]
  (.appendMessages (get-folder store folder)
                   (into-array Message messages)))

(defn append-emails [store emails]
  (doall
    (pmap (fn [[folder emails]]
            (let [folder-str (str "RSS2/" (name folder))]
              (create store folder-str)
              (info "Appending" (count emails) "emails in to the folder" folder-str)
              (append store folder-str emails)))
          emails)))
