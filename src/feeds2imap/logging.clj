(ns feeds2imap.logging
  (:require [clojure.core.typed :refer [ann Seq Agent1 Any check-ns cf All]]))

(ann logger (Agent1 nil))
(def logger (agent nil))

(ann ^:no-check prn-stdout [nil Any -> nil])
(defn prn-stdout [_ args]
  (apply println args))

(ann ^:no-check prn-stderr [nil Any -> nil])
(defn prn-stderr [_ args]
  (binding [*out* *err*]
    (apply println args)))

(ann info [Any * -> (Agent1 nil)])
(defn info [& args]
  (send logger prn-stdout args))

(ann error [Any * -> (Agent1 nil)])
(defn error [& args]
  (send logger prn-stderr args))

(ann wait [-> Boolean])
(defn wait []
  (await-for 3000 logger))
