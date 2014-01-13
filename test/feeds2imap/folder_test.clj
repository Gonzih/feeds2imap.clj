(ns feeds2imap.folder-test
  (:require [midje.sweet :refer :all]
            [feeds2imap.folder :refer :all]
            [clojure.core.typed :refer [check-ns]]))

(fact "about types"
      (check-ns 'feeds2imap.folder) => :ok)
