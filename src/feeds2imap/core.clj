(ns feeds2imap.core
  (:gen-class)
  (:require [feeds2imap.types :as types]
            [feeds2imap.feeds :as feeds]
            [feeds2imap.settings :as settings]
            [feeds2imap.imap :as imap]
            [feeds2imap.folder :as folder]
            [feeds2imap.macro :refer :all]
            [feeds2imap.opml :as ompl]
            [feeds2imap.db :as db]
            [feeds2imap.logging :as logging :refer [info error]]
            [clojure.pprint :refer [pprint]]
            [clojure.core.match :refer [match]]
            [clojure.spec.alpha :as s])
  (:import [java.net NoRouteToHostException UnknownHostException]
           [javax.mail MessagingException]
           [java.io File]
           [java.lang NullPointerException]))

(set! *warn-on-reflection* true)

(defn pull []
  (try*
    (db/init-db!)
    (let [{:keys [username password host port to from]} (settings/imap)
          urls         (settings/urls)
          _            (info "Found" (count urls) "folders in urls.")
          new-items    (feeds/new-items urls)
          _            (info "Found" (count new-items)
                             "folder(s) with" (->> new-items (map second) flatten count)
                             "new item(s) in total.")
          imap-session (imap/get-session (imap/get-props) nil)
          imap-store   (imap/get-store imap-session)
          emails       (feeds/to-emails imap-session from to new-items)]
      (when-not (empty? new-items)
        (with-open [store imap-store]
          (info "Connecting to imap host.")
          (imap/connect store host port username password)
          (info "Appending emails.")
          (folder/append-emails store emails)
          (info "Updating cache.")
          (db/update-cache! (feeds/extract-guids new-items)))))
    (catch* [UnknownHostException NoRouteToHostException MessagingException] e
            (info "Exception in pull" e))))

(defn sleep [ms]
  (Thread/sleep ms))

(defn pull-with-catch []
  (info "Running pull in future")
  (try
    (pull)
    (catch Exception e
      (info "Exception in pull call inside auto" e))))

(defn auto []
  (loop [previous-task nil]
    (when (and (future? previous-task)
               (not (future-done? previous-task)))
      (info "Cancelling previous future")
      (future-cancel previous-task))
    (let [delay-str (System/getenv "DELAY")
          minutes (if delay-str (Integer. delay-str) 60)
          current-task (future-call pull-with-catch)]
      (info "Sleeping in auto for" minutes "minutes")
      (sleep (* minutes 60 1000))
      (recur current-task))))

(defn add [folder url]
  {:pre [(s/valid? :feeds2imap.types/keyword folder) (s/valid? :feeds2imap.types/string url)]}
  (let [folder (keyword folder)
        urls (settings/urls)
        folder-urls (or (get urls folder) [])]
    (settings/urls (assoc urls folder (conj folder-urls url)))))

(defn shutdown-agents-with-try []
  (try*
   (shutdown-agents)
   (catch* [NullPointerException] e
           (info "Exception while shuting down agents" e))))

(defn show [] (pprint (settings/urls)))

(defn -main
  [& args]
  (match args
    ([] :seq) (pull)
    (["pull"] :seq) (pull)
    (["show"] :seq) (show)
    (["auto"] :seq) (auto)
    (["imap" "encrypt"]   :seq) (settings/encrypt-imap!)
    (["imap" "decrypt"]   :seq) (settings/decrypt-imap!)
    (["opml2clj" file]    :seq) (ompl/convert-and-print-from-file! file)
    (["add" folder url]   :seq) (do (add folder url) (show))
    (["opml2clj" from to] :seq) (ompl/convert-and-write->file! from to)
    :else (error "Can't handle arguments" args))
  (logging/wait)
  (shutdown-agents-with-try))
