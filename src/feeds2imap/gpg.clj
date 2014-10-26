(ns feeds2imap.gpg
  (:require [clojure.java.shell :as shell]
            [clojure.core.typed :refer [ann]]
            [feeds2imap.types :refer :all]))

(ann gpg-program String)
(def gpg-program "gpg")

(ann gpg-programm [String * -> ShellResult])
(defn gpg
  "Shells out to (gpg-program) with the given arguments"
  [& args]
  (try
    (apply shell/sh gpg-program args)
    (catch java.io.IOException e
      {:exit 1 :err (.getMessage e)})))

(ann gpg-available? [-> Boolean])
(defn gpg-available?
  "Verifies (gpg-program) exists"
  []
  (zero? (:exit (gpg "--version"))))
