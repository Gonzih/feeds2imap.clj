(ns feeds2imap.annotations
  (:require [clojure.core.typed :refer :all]
            [feeds2imap.types :refer :all]
            [clojure.pprint]
            [hiccup.compiler]
            [feedparser-clj.core]
            [clojure.tools.logging]
            [clojure.edn]
            [clojure.xml]
            [digest])
  (:import  [clojure.lang Keyword]
            [java.io File]))

(ann clojure.pprint/pprint [Any -> nil])
(ann clojure.core/print-str [Any * -> String])

(ann ^:no-check clojure.core/slurp [String -> String])
(ann ^:no-check clojure.core/spit  [String String -> nil])

(ann ^:no-check feedparser-clj.core/parse-feed [String -> ParsedFeed])

(ann ^:no-check hiccup.compiler/render-html [(Option Any) -> String])
(ann ^:no-check hiccup.compiler/render-attr-map [(Option Any) -> String])

(ann ^:no-check clojure.tools.logging/info  [Any * -> nil])
(ann ^:no-check clojure.tools.logging/error [Any * -> nil])
(ann ^:no-check clojure.tools.logging/log* [Any * -> nil])
(ann ^:no-check clojure.tools.logging.impl/enabled? [Any * -> Boolean])
(ann ^:no-check clojure.tools.logging.impl/get-logger [Any * -> Any])
(ann ^:no-check clojure.tools.logging/*logger-factory* [Any * -> Any])

(ann ^:no-check clojure.xml/parse [File -> (Map Keyword XML)])

(ann ^:no-check digest/md5 [String -> String])
