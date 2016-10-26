(ns feeds2imap.opml-test
  (:require [feeds2imap.opml :refer :all]
            [feeds2imap.test-helpers :refer [spec-fn]]
            [clojure.spec :as s]
            [clojure.spec.test :as stest]
            [clojure.test :refer [deftest is]]))

(deftest testing-specs
  (doseq [fname (stest/enumerate-namespace 'feeds2imap.opml)]
    (is (spec-fn fname))))
