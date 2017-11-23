(ns feeds2imap.imap
  (:require [feeds2imap.folder :as folder]
            [feeds2imap.message :as message]
            [clojure.spec.alpha :as s])
  (:import [javax.mail Session Store Authenticator]
           [java.net UnknownHostException]
           [java.util Properties]))

(s/def ::properties (partial instance? Properties))
(s/def ::authenticator (partial instance? Authenticator))
(s/def ::store (partial instance? Store))
(s/def ::session (partial instance? Session))

(s/fdef get-pros
        :args (s/cat)
        :ret ::properties)

(defn ^Properties get-props []
  (doto (Properties.)
    (.put "mail.store.protocol" "imap")
    (.put "mail.imap.starttls.enable" "true")
    (.put "mail.imap.socketFactory.class" "javax.net.ssl.SSLSocketFactory")))

(s/fdef get-session
        :args (s/cat :properties ::properties :authenticator ::authenticator))

(defn ^Session get-session [^Properties props authenticator]
  (Session/getInstance props authenticator))

(s/fdef get-store
        :args (s/cat :session ::session)
        :ret ::store)

(defn ^Store get-store [^Session session]
  (.getStore session))

(s/fdef connect
        :args (s/cat :store ::store
                     :host :feeds2imap.types/string
                     :port :feeds2imap.types/string
                     :username :feeds2imap.types/string
                     :password :feeds2imap.types/string)
        :ret ::store)

(defn ^Store connect [^Store store host port username password]
  (when (some nil? [host port username password])
    (throw (UnknownHostException. "Put IMAP settings in ~/.config/feeds2imap.clj/imap.clj")))
  (.connect store host port username password)
  store)
