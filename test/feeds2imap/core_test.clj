(ns feeds2imap.core-test
  (:require [clojure.test :refer :all]
            [feeds2imap.core :refer :all]
            [clojure.core.typed :refer [check-ns]]))

(deftest typed
  (testing "Types"
    (is (check-ns 'feeds2imap.core))))
