(ns feeds2imap.test-helpers
  (:require  [clojure.test :as t]
             [clojure.spec.test :as stest]))

(defn read-num-tests-from-env []
  (if-let [s (System/getenv "NUM_TESTS")]
    (Integer/parseInt s)))

(defn num-tests []
  (or (read-num-tests-from-env)
      100))

(defn be-successful-check-run [{:keys [check-threw check-failed] :as summary} fname]
  (let [success? (and (nil? check-threw)
                      (or (nil? check-failed)
                          (zero? check-failed)))]
    (if success?
      true
      (do
        (println "Failed:" fname "->" summary)
        false))))

(defn spec-fn [fname]
  (println "Testing:" fname)
  (-> fname
      (stest/check {:clojure.spec.test.check/opts {:num-tests (num-tests)}})
      stest/summarize-results
      (be-successful-check-run fname)))
