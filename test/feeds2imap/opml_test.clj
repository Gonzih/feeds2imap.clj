(ns feeds2imap.opml-test
  (:require [clojure.test :refer :all]
            [feeds2imap.opml :refer :all]
            [clojure.core.typed :refer [check-ns]]))

(deftest typed
  (testing "Types"
    (is (check-ns 'feeds2imap.opml))))
