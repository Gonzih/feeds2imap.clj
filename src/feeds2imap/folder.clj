(ns feeds2imap.folder
  (:require [feeds2imap.logging :refer [info error]]
            [feeds2imap.types]
            [clojure.spec.alpha :as s])
  (:import  [javax.mail Store Folder Message]
            [javax.mail.internet MimeMessage]))

(s/def ::folder (partial instance? Folder))
(s/def ::store (partial instance? Store))

(s/fdef get-folder
        :args (s/cat :store ::store :folder-str :feeds2imap.types/string)
        :ret ::folder)

(defn ^Folder get-folder [^Store store ^String folder]
  (.getFolder store folder))

(s/fdef exists
        :args (s/cat :store ::store :folder ::folder)
        :ret :feeds2imap.types/boolean)

(defn exists [^Store store folder]
  (.exists (get-folder store folder)))

(s/fdef create
        :args (s/cat :store ::store :folder ::folder)
        :ret :feeds2imap.types/boolean)

(defn create [^Store store folder]
  (when-not (exists store folder)
    (info "Creating IMAP folder" folder)
    (.create (get-folder store folder) Folder/HOLDS_MESSAGES)))

(s/fdef append
        :args (s/cat :store ::store :folder ::folder :messages :feeds2imap.types/mime-messages)
        :ret nil?)

(defn append [store folder messages]
  (.appendMessages (get-folder store folder)
                   (into-array Message messages)))

(s/fdef append-emails
        :args (s/cat :store ::store :emails :feeds2imap.types/mime-messages-with-folder))

(defn append-emails [store emails]
  (let [groupped (group-by :folder emails)]
    (doall
      (pmap
        (fn [[folder emails]]
          (let [folder-str (str "RSS/" (name folder))
                msgs (map :mime-message emails)]
            (create store folder-str)
            (info "Appending" (count msgs) "emails in to the IMAP folder" folder-str)
            (append store folder-str msgs)))
        groupped))))
