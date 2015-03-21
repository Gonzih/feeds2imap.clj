(ns feeds2imap.types
  (:require [clojure.core.typed :refer [defalias Set HMap Seqable Vec Map Keyword Option Any TFn Num]])
  (:import [javax.mail.internet MimeMessage]
           [java.util Date]))

(defalias Cache (Map String Num))

(defalias Item
  (HMap :mandatory {:authors (Seqable String)
                    :title String
                    :link String
                    :contents (Seqable (HMap :optional {:value String}))
                    :description (HMap :optional {:value String})}))

(defalias Items (Seqable Item))
(defalias UnflattenedItems (Seqable Items))

(defalias ParsedFeed
  (HMap :mandatory {:entries Items}))

(defalias Message MimeMessage)
(defalias Messages (Seqable Message))

(defalias Urls (Vec String))

(defalias Folder
  (TFn [[x :variance :covariant]] (Map Keyword x)))

(defalias MessageMap
  (HMap :mandatory {:from    String
                    :to      String
                    :subject String
                    :html    String}
        :optional  {:date (Option Date)}))

(defalias XML (Map Keyword Any))

(defalias ImapConfiguration
  (HMap :mandatory {:host String
                    :port Num
                    :username String
                    :password String}
        :optional {:to String
                   :from String}))

(defalias ShellResult
  (HMap :mandatory {:exit Num}
        :optional {:err String
                   :out String}))
