(ns feeds2imap.feeds-test
  (:require [clojure.test :refer :all]
            [feeds2imap.feeds :refer :all]
            [clojure.core.typed :refer [check-ns]]))

(deftest typed
  (testing "Types"
    (is (check-ns 'feeds2imap.feeds))))

(deftest reduce-new-items-test
  (testing "Properly calculate new items and update cache"
    (is (= (reduce-new-items #{"a" "b"}
                             {:b [{:uri "c"} {:uri "d"}]
                              :a [{:uri "b"} {:uri "z"}]})
           {:cache #{"a" "b" "c" "d" "z"}
            :new-items {:b [{:uri "d"} {:uri "c"}]
                        :a [{:uri "z"}]}}))))
