(ns feeds2imap.core-test
  (:require [midje.sweet :refer :all]
            [feeds2imap.core :refer :all]
            [feeds2imap.test-helpers :refer :all]))

(fact "about types"
      (check-ns-quiet 'feeds2imap.core) => :ok)
