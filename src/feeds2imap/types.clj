(ns feeds2imap.types
  (:require [clojure.core.typed :refer :all])
  (:import [clojure.lang Keyword]
           [javax.mail.internet MimeMessage]))

(def-alias Cache (Set String))

(def-alias Item
  (HMap :mandatory {:authors (Seqable String)
                    :title String
                    :link String
                    :contents (Seqable (HMap :optional {:value String}))
                    :description (HMap :optional {:value String})}))

(def-alias Items (Seqable Item))
(def-alias UnflattenedItems (Seqable Items))

(def-alias ParsedFeed
  (HMap :mandatory {:entries Items}))

(def-alias Message MimeMessage)
(def-alias Messages (Seqable Message))

(def-alias Urls (Vec String))

(def-alias Folder
  (TFn [[x :variance :covariant]] (Map Keyword x)))
