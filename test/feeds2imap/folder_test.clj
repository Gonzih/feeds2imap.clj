(ns feeds2imap.folder-test
  (:require [midje.sweet :refer :all]
            [feeds2imap.folder :refer :all]
            [feeds2imap.utils :refer :all]))

(fact "about types"
      (check-ns-quiet 'feeds2imap.folder) => :ok)
