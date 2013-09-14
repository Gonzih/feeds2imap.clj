(ns feeds2imap.message
  (:require [clojure.core.typed :refer :all]
            [feeds2imap.types :refer :all])
  (:import [javax.mail Message$RecipientType Session]
           [javax.mail.internet MimeMessage InternetAddress MimeBodyPart]
           [java.util Date]))

(non-nil-return javax.mail.Message$RecipientType/TO :all)

(ann recipient-type-to [-> Message$RecipientType])
(defn ^Message$RecipientType recipient-type-to
  []
  {:post [%]}
  (Message$RecipientType/TO))

(ann from-map [Session MessageMap -> Message])
(defn ^MimeMessage from-map
  "Create message from map."
  [^Session session {:keys [from ^String to subject html date]}]
  (let [message (MimeMessage. session)]
    (doto message
      (.setFrom (InternetAddress. from))
      (.setRecipients (recipient-type-to) to)
      (.setSubject subject)
      (.setContent html "text/html; charset=utf-8"))
    (when date
        (.setSentDate message date))
    message))
