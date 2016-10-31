(ns feeds2imap.settings-test
  (:require [feeds2imap.settings :refer :all]
            [feeds2imap.test-helpers :refer [spec-fn]]
            [clojure.spec :as s]
            [clojure.spec.test :as stest]
            [clojure.test :refer [deftest is]]))

(deftest testing-specs
  (doseq [fname (disj (stest/enumerate-namespace 'feeds2imap.settings)
                      'feeds2imap.settings/urls
                      'feeds2imap.settings/imap
                      'feeds2imap.settings/unencrypted-imap
                      'feeds2imap.settings/encrypted-imap)]
    (is (spec-fn fname))))
