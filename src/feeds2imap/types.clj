(ns feeds2imap.types
  (:require [clojure.core.typed :refer [def-alias Set HMap Seqable Vec Map Keyword Option Any]])
  (:import [javax.mail.internet MimeMessage]
           [java.util Date]))

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

(def-alias MessageMap
  (HMap :mandatory {:from    String
                    :to      String
                    :subject String
                    :html    String}
        :optional  {:date (Option Date)}))

(def-alias XML (Map Keyword Any))
