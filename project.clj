(defproject feeds2imap "0.2.0"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :core.typed {:check [feeds2imap.core
                       feeds2imap.feeds
                       feeds2imap.folder
                       feeds2imap.imap
                       feeds2imap.message
                       feeds2imap.settings]}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [javax.mail/mail "1.4.7"]
                 [org.clojars.scsibug/feedparser-clj "0.4.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [hiccup "1.0.4"]
                 [org.clojure/tools.logging "0.2.6"]
                 [log4j/log4j "1.2.17"]
                 [digest "1.4.3"]]
  :main feeds2imap.core
  :profiles {:dev {:dependencies [[org.clojure/core.typed "0.2.10"]
                                  [midje "1.6.0"]]}}
  :min-lein-version "2.0.0")
