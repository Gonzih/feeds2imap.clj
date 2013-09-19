(ns feeds2imap.imap-test
  (:require [clojure.test :refer :all]
            [feeds2imap.imap :refer :all]
            [clojure.core.typed :refer [check-ns]]))

(deftest typed
  (testing "Types"
    (is (check-ns 'feeds2imap.imap))))
