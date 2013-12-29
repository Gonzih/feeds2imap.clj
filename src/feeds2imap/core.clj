(ns feeds2imap.core
  (:gen-class)
  (:require [feeds2imap.feeds :as feeds]
            [feeds2imap.settings :as settings]
            [feeds2imap.imap :as imap]
            [feeds2imap.folder :as folder]
            [feeds2imap.macro :refer :all]
            [feeds2imap.opml :refer [convert-opml]]
            [feeds2imap.annotations :refer :all]
            [clojure.tools.logging :refer [info error]]
            [clojure.pprint :refer [pprint]]
            [clojure.core.typed :refer :all]
            [clojure.java.io :refer [file writer]])
  (:import [java.net NoRouteToHostException UnknownHostException]
           [javax.mail MessagingException]
           [clojure.lang Keyword]
           [java.io File]))

(set! *warn-on-reflection* true)

(ann ^:no-check pull [-> Any])
(defn pull []
  (try*
    (let [{:keys [username password host port to from]} (settings/imap)
          imap-session  (imap/get-session (imap/get-props) nil)
          imap-store    (imap/get-store imap-session)
          cache         (settings/read-items)
          _             (info "Found" (count cache) "items in cache.")
          urls          (settings/urls)
          _             (info "Found" (count urls) "folders in urls.")
          new-items     (feeds/new-items cache urls)
          _             (info "Found" (count new-items) "new items.")
          emails        (feeds/to-emails imap-session from to new-items)]

      (when-not (empty? emails)
        (with-open [store imap-store]
          (info "Connecting to imap host.")
          (imap/connect store host port username password)
          (info "Appending emails.")
          (folder/append-emails store emails)
          (info "Updating cache.")
          (settings/write-items (feeds/mark-all-as-read cache new-items)))))
      (catch* [UnknownHostException NoRouteToHostException MessagingException] e
              (info "Exception in pull" e))))

(ann sleep [Long -> nil])
(defn sleep [ms]
  (Thread/sleep ms))

(ann ^:no-check auto [-> Any])
(defn auto []
  (pull)
  (sleep (* 60 60 1000))
  (recur))

(ann add [Keyword String -> Any])
(defn add [folder url]
  (let [folder (keyword folder)
        urls (settings/urls)
        folder-urls (or (get urls folder) [])]
    (settings/urls (assoc urls folder (conj folder-urls url)))))

(ann show [-> nil])
(defn show [] (pprint (settings/urls)))

(ann ^:no-check -main [Any -> Any])
(defn -main
  ([]
    (pull)
    (shutdown-agents))
  ([command]
    (case command
          "auto" (auto)
          "show" (show)
          "pull" (pull))
    (shutdown-agents))
  ([command arg]
     (case command
       "opml2clj" (->> (File. ^String arg)
                       convert-opml
                       pprint)))
  ([command arg1 arg2]
    (case command
          "add" (do (add arg1 arg2) (show))
          "opml2clj" (let [w (writer (file arg2))
                           m (->> (File. ^String arg1)
                                  convert-opml)]
                       (pprint m w)))
    (shutdown-agents)))
