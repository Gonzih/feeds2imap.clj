(ns feeds2imap.gpg
  (:require [clojure.java.shell :as shell]
            [clojure.spec :as s]))

(def gpg-program "gpg")

(s/fdef gpg
        :args (s/* :feeds2imap.types/string)
        :ret :feeds2imap.types/shell-result)
(defn gpg
  "Shells out to (gpg-program) with the given arguments"
  [& args]
  (try
    (apply shell/sh gpg-program args)
    (catch java.io.IOException e
      {:exit 1 :err (.getMessage e)})))

(s/fdef gpg-available?
        :args (s/cat)
        :ret :feeds2imap.types/boolean)

(defn gpg-available?
  "Verifies (gpg-program) exists"
  []
  (zero? (:exit (gpg "--version"))))
