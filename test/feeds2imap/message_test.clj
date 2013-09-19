(ns feeds2imap.message-test
  (:require [clojure.test :refer :all]
            [feeds2imap.message :refer :all]
            [clojure.core.typed :refer [check-ns]]))

(deftest typed
  (testing "Types"
    (is (check-ns 'feeds2imap.message))))
