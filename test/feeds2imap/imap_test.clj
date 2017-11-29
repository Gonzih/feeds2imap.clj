(ns feeds2imap.imap-test
  (:require [feeds2imap.imap :refer :all]
            [feeds2imap.test-helpers :refer [spec-fn]]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.test :refer [deftest is]]))

(deftest testing-specs
  (doseq [fname (disj (stest/enumerate-namespace 'feeds2imap.imap)
                      `connect `get-store `get-props)]
    (is (spec-fn fname))))
