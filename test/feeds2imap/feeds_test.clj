(ns feeds2imap.feeds-test
  (:require [clojure.test :refer :all]
            [feeds2imap.feeds :refer :all]
            [clojure.core.typed :refer [check-ns]]))

(deftest typed
  (testing "Types"
    (is (check-ns 'feeds2imap.feeds))))
