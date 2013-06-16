(ns feeds2imap.folder
  (:import [javax.mail Folder Message]))

(defn get-folder [store folder]
  (-> store
      (.getFolder folder)))

(defn exists [store folder]
  (-> (get-folder store folder)
      .exists))

(defn create [store folder]
  (when-not (exists store folder)
    (-> (get-folder store folder)
        (.create Folder/HOLDS_MESSAGES))))

(defn append [store folder messages]
  (.appendMessages (get-folder store folder)
                   (into-array Message messages)))

(defn append-emails [store emails]
  (doall
    (map (fn [[folder emails]]
           (let [folder-str (str "RSS2/" (name folder))]
             (create store folder-str)
             (append store folder-str emails)))
         emails)))
