(ns feeds2imap.imap-test
  (:require [midje.sweet :refer :all]
            [feeds2imap.imap :refer :all]
            [feeds2imap.utils :refer :all]))

(fact "about types"
      (check-ns-quiet 'feeds2imap.imap) => :ok)
