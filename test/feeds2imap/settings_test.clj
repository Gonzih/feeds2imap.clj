(ns feeds2imap.settings-test
  (:require [midje.sweet :refer :all]
            [feeds2imap.settings :refer :all]
            [feeds2imap.utils :refer :all]))

(fact "about types"
      (check-ns-quiet 'feeds2imap.settings) => :ok)
