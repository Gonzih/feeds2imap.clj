(ns feeds2imap.annotations
  (:require [clojure.core.typed :refer [ann Any Option Map Keyword U IFn]]
            [feeds2imap.types :refer :all]
            [clojure.pprint]
            [hiccup.compiler]
            [feedparser-clj.core]
            [clojure.edn]
            [clojure.xml]
            [digest]
            [clojure.java.shell]
            [clojure.java.io])
  (:import [java.io File BufferedWriter]))

(ann ^:no-check clojure.pprint/pprint (IFn [Any -> nil]
                                           [Any BufferedWriter -> nil]))
(ann ^:no-check clojure.core/print-str [Any * -> String])

(ann ^:no-check clojure.core/slurp [String -> String])
(ann ^:no-check clojure.core/spit  [String String -> nil])

(ann ^:no-check feedparser-clj.core/parse-feed [String -> ParsedFeed])

(ann ^:no-check hiccup.compiler/render-html [(Option Any) -> String])
(ann ^:no-check hiccup.compiler/render-attr-map [(Option Any) -> String])

(ann ^:no-check clojure.xml/parse [File -> (Map Keyword XML)])

(ann ^:no-check digest/md5 [String -> String])

(ann ^:no-check clojure.core/push-thread-bindings [Any -> Any])
(ann ^:no-check clojure.core/hash-map [Any * -> Map])

(ann ^:no-check clojure.java.shell/sh [String * -> ShellResult])

(ann ^:no-check clojure.java.io/file   [String -> File])
(ann ^:no-check clojure.java.io/writer (IFn [File -> BufferedWriter]
                                            [String -> BufferedWriter]))

(ann ^:no-check clojure.edn/read-string [(U nil String) -> Any])
