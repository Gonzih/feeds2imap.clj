(ns feeds2imap.logging
  (:require [clojure.tools.logging :as log]
            [clojure.core.typed :refer [ann Any]]))

(def logger (agent nil))

; (ann prn-stdout [[Any] -> nil])
(defn prn-stdout [_ args]
  (apply println args))

; (ann prn-stderr [[Any] -> nil])
(defn prn-stderr [_ args]
  (binding [*out* *err*]
    (apply println args)))

(ann info [Any * -> nil])
(defn info [& args]
  (send logger prn-stdout args))

(ann error [Any * -> nil])
(defn error [& args]
  (send logger prn-stderr args))
