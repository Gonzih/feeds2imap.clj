(defproject feeds2imap "0.4.0"
  :description "Pull RSS/Atom feeds to your IMAP folders with Clojure on JVM."
  :url "https://github.com/Gonzih/feeds2imap.clj"
  :license {:name "The MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0-RC2"]
                 [javax.mail/mail "1.4.7"]
                 [org.clojars.gnzh/feedparser-clj "0.6.0" :exclusions [org.clojure/clojure]]
                 [org.clojure/data.codec "0.1.1"]
                 [hiccup "1.0.5"]
                 [digest "1.4.6"]
                 [org.clojure/core.match "0.2.2"]
                 [org.clojure/java.jdbc "0.7.3"]
                 [org.xerial/sqlite-jdbc "3.21.0"]]
  :main feeds2imap.core
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]]}
             :uberjar {:aot :all}}
  :min-lein-version "2.0.0")
