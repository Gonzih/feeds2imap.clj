(ns feeds2imap.settings-test
  (:require [clojure.test :refer :all]
            [feeds2imap.settings :refer :all]
            [clojure.core.typed :refer [check-ns]]))

(deftest typed
  (testing "Types"
    (is (check-ns 'feeds2imap.settings))))
