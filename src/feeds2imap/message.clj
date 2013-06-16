(ns feeds2imap.message
  (:import [javax.mail Message$RecipientType]
           [javax.mail.internet MimeMessage InternetAddress MimeBodyPart]
           [java.util Date]))

(defn from-map
  "Create message from map."
  [session {:keys [from to subject html]}]
  (let [message (MimeMessage. session)]
    (doto message
      (.setFrom (InternetAddress. from))
      (.setRecipients Message$RecipientType/TO to)
      (.setSubject subject)
      (.setContent html "text/html; charset=utf-8")
      (.setSentDate (Date.)))))
