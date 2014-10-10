(ns feeds2imap.logging
  (:require [clojure.tools.logging :as log]
            [clojure.core.typed :refer [ann Any]]))

(ann info [Any * -> nil])
(defmacro info [& args]
  `(log/info ~@args))

(ann error [Any * -> nil])
(defmacro error [& args]
  `(log/error ~@args))
