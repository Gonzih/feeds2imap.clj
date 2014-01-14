(ns feeds2imap.utils
  (:require [clojure.core.typed :refer [check-ns]]))

(defn check-ns-quiet [ns-symbol]
  (let [result (atom nil)]
    (with-out-str
      (let [return-value (check-ns ns-symbol)]
        (reset! result return-value)))
    @result))
