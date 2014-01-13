(ns feeds2imap.opml-test
  (:require [midje.sweet :refer :all]
            [feeds2imap.opml :refer :all]
            [clojure.core.typed :refer [check-ns]]))

(fact "about types"
      (check-ns 'feeds2imap.opml) => :ok)
