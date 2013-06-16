(ns feeds2imap.imap
  (:require [feeds2imap.folder :as folder]
            [feeds2imap.message :as message])
  (:import [javax.mail Session]
           [java.util Properties]))

(defn get-props []
  (doto (Properties.)
    (.put "mail.store.protocol" "imap")
    (.put "mail.imap.starttls.enable" "true")
    (.put "mail.imap.socketFactory.class" "javax.net.ssl.SSLSocketFactory")))

(defn get-session [props authenticator]
  (Session/getInstance props authenticator))

(defn get-store [session]
  (.getStore session))

(defn connect [store host port username password]
  (.connect store host port username password)
  store)
