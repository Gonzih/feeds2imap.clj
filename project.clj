(defproject feeds2imap "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [javax.mail/mail "1.4.7"]
                 [org.clojars.scsibug/feedparser-clj "0.4.0"]
                 [hiccup "1.0.3"]
                 [org.clojure/tools.logging "0.2.6"]]
  :main feeds2imap.core)
