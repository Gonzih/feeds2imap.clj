(ns feeds2imap.folder-test
  (:require [feeds2imap.folder :refer :all]
            [feeds2imap.test-helpers :refer [spec-fn]]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.test :refer [deftest is]]))

(deftest testing-specs
  (doseq [fname (disj (stest/enumerate-namespace 'feeds2imap.folder)
                      `get-folder `create `append `append-emails `exists)]
    (is (spec-fn fname))))
