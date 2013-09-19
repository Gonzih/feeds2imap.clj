(ns feeds2imap.folder-test
  (:require [clojure.test :refer :all]
            [feeds2imap.folder :refer :all]
            [clojure.core.typed :refer [check-ns]]))

(deftest typed
  (testing "Types"
    (is (check-ns 'feeds2imap.folder))))
