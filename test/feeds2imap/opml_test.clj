(ns feeds2imap.opml-test
  (:require [midje.sweet :refer :all]
            [feeds2imap.opml :refer :all]
            [feeds2imap.utils :refer :all]))

(fact "about types"
      (check-ns-quiet 'feeds2imap.opml) => :ok)
