(ns feeds2imap.core-test
  (:require [feeds2imap.core]
            [feeds2imap.test-helpers :refer [spec-fn]]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.test :refer [deftest is]]))

(deftest testing-specs
  (doseq [fname (stest/enumerate-namespace 'feeds2imap.core)]
    (is (spec-fn fname))))
