(ns feeds2imap.db
  (:require [clojure.java.jdbc :refer :all :as jdbc]
            [feeds2imap.settings :refer [db-path]]))

(defn db-spec []
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname (db-path)})

(def creat-db-query
  "CREATE TABLE IF NOT EXISTS feeds (guid STRING NOT NULL PRIMARY KEY);
   CREATE INDEX IF NOT EXISTS guid_index ON feeds (guid);")

(defn init-db! []
  (jdbc/execute! (db-spec) creat-db-query))

(defn is-new? [guid]
  (-> (jdbc/query (db-spec) ["SELECT * FROM feeds WHERE guid = ?" guid])
      seq not))

(defn update-cache! [guids]
  (jdbc/insert-multi! (db-spec) :feeds
                      (map (fn [guid] {:guid guid}) guids)))
