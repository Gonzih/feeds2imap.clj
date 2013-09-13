(ns feeds2imap.imap
  (:require [feeds2imap.folder :as folder]
            [feeds2imap.message :as message]
            [clojure.core.typed :refer :all])
  (:import [javax.mail Session Store Authenticator]
           [java.net UnknownHostException]
           [java.util Properties]))

(non-nil-return javax.mail.Session/getStore :all)
(non-nil-return javax.mail.Session/getInstance :all)

(ann get-props [-> Properties])
(defn ^Properties get-props []
  (doto (Properties.)
    (.put "mail.store.protocol" "imap")
    (.put "mail.imap.starttls.enable" "true")
    (.put "mail.imap.socketFactory.class" "javax.net.ssl.SSLSocketFactory")))

(ann get-session [Properties Authenticator -> Session])
(defn ^Session get-session [^Properties props authenticator]
  (Session/getInstance props authenticator))

(ann get-store [Session -> Store])
(defn ^Store get-store [^Session session]
  (.getStore session))

(ann connect [Store String int String String -> Store])
(defn ^Store connect [^Store store host port username password]
  (when (some nil? [host port username password])
    (throw (UnknownHostException. "Put IMAP settings in ~/.config/feeds2imap.clj/imap.clj")))
  (.connect store host port username password)
  store)
