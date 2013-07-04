(ns feeds2imap.core
  (:gen-class)
  (:require [feeds2imap.feeds :as feeds]
            [feeds2imap.settings :as settings]
            [feeds2imap.imap :as imap]
            [feeds2imap.folder :as folder]
            [feeds2imap.macro :refer :all]
            [clojure.tools.logging :refer [info error]]
            [clojure.pprint :refer [pprint]])
  (:import [java.net NoRouteToHostException UnknownHostException]))

(set! *warn-on-reflection* true)

(defn pull []
  (let [{:keys [username password host port to from]} (settings/imap)
        imap-session  (imap/get-session (imap/get-props) nil)
        imap-store    (imap/get-store imap-session)
        cache         (settings/read-items)
        urls          (settings/urls)
        new-items     (feeds/new-items cache urls)
        emails        (feeds/to-emails imap-session from to new-items)]
    (try*
      (with-open [store imap-store]
        (imap/connect store host port username password)
        (folder/append-emails store emails)
        (settings/write-items (feeds/mark-all-as-read cache new-items)))
      (catch* [UnknownHostException NoRouteToHostException] e
              (info "Exception in pull" e)))))

(defn sleep [ms]
  (Thread/sleep ms))

(defn auto []
  (pull)
  (sleep (* 60 60 1000))
  (recur))

(defn add [folder url]
  (let [folder (keyword folder)
        urls (settings/urls)
        folder-urls (or (get urls folder) [])]
    (settings/urls (assoc urls folder (conj folder-urls url)))))

(defn show [] (pprint (settings/urls)))

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
  ([command arg1 arg2]
    (case command
          "add" (do (add arg1 arg2) (show)))
    (shutdown-agents)))
