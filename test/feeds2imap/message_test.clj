(ns feeds2imap.message-test
  (:require [midje.sweet :refer :all]
            [feeds2imap.message :refer :all]
            [clojure.core.typed :refer [check-ns]]))

(fact "about types"
      (check-ns 'feeds2imap.message) => :ok)
