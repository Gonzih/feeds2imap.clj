(ns feeds2imap.core
  (:gen-class)
  (:require [feeds2imap.feeds :as feeds]
            [feeds2imap.settings :as settings]
            [feeds2imap.imap :as imap]
            [feeds2imap.folder :as folder]))

(defn pull []
  (let [{:keys [username password host port to from]} (settings/imap)
        imap-session  (imap/get-session (imap/get-props) nil)
        imap-store    (imap/get-store imap-session)
        cache         (settings/read-items)
        urls          (settings/urls)
        new-items     (feeds/new-items cache urls)
        emails        (feeds/to-emails imap-session from to new-items)]
    (with-open [store imap-store]
      (imap/connect store host port username password)
      (folder/append-emails store emails)
      (settings/write-items (feeds/mark-all-as-read cache new-items)))))

(defn -main [& x]
  (time (pull)))
