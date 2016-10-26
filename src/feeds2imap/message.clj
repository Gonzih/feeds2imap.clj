(ns feeds2imap.message
  (:require [clojure.spec :as s])
  (:import [javax.mail Message$RecipientType Session]
           [javax.mail.internet MimeMessage InternetAddress MimeBodyPart]
           [java.util Date]))

(s/def ::session (partial instance? Session))
(s/def ::recipient-to (partial instance? Message$RecipientType))

(s/fdef recipient-type-to
        :args (s/cat)
        :ret ::recipient-to)

(defn ^Message$RecipientType recipient-type-to
  []
  {:post [%]}
  (Message$RecipientType/TO))

(s/fdef from-map
        :args (s/cat :session ::session :message :feeds2imap.types/message)
        :ret :feeds2imap.types/mime-message)

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
