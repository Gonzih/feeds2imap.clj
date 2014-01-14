(ns feeds2imap.message-test
  (:require [midje.sweet :refer :all]
            [feeds2imap.message :refer :all]
            [feeds2imap.test-helpers :refer :all]))

(fact "about types"
      (check-ns-quiet 'feeds2imap.message) => :ok)
