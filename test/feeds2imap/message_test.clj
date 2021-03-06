(ns feeds2imap.message-test
  (:require [feeds2imap.message :refer :all]
            [feeds2imap.test-helpers :refer [spec-fn]]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.test :refer [deftest is]]))

(deftest testing-specs
  (doseq [fname (disj (stest/enumerate-namespace 'feeds2imap.message)
                      `recipient-type-to)]
    (is (spec-fn fname))))
