(ns feeds2imap.imap-test
  (:require [midje.sweet :refer :all]
            [feeds2imap.imap :refer :all]
            [clojure.core.typed :refer [check-ns]]))

(fact "about types"
      (check-ns 'feeds2imap.imap) => :ok)
