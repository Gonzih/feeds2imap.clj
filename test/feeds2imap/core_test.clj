(ns feeds2imap.core-test
  (:require [midje.sweet :refer :all]
            [feeds2imap.core :refer :all]
            [clojure.core.typed :refer [check-ns]]))

(fact "about types"
      (check-ns 'feeds2imap.core) => :ok)
