(ns feeds2imap.settings-test
  (:require [midje.sweet :refer :all]
            [feeds2imap.settings :refer :all]
            [clojure.core.typed :refer [check-ns]]))

(fact "about types"
      (check-ns 'feeds2imap.settings) => :ok)
