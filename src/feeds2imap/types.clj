(ns feeds2imap.types
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen])
  (:import [javax.mail.internet MimeMessage]
           [javax.mail Session]
           [java.util Date]))

(s/def ::int  int?)
(s/def ::string  string?)
(s/def ::keyword keyword?)
(s/def ::boolean boolean?)

(s/def ::type  ::string)
(s/def ::value ::string)
(s/def ::title ::string)
(s/def ::link  ::string)
(s/def ::email (s/nilable ::string))
(s/def ::name  (s/nilable ::string))
(s/def ::uri   (s/nilable ::string))

(s/def ::author (s/keys :opt-un [::email ::name ::uri]))

(s/def ::authors (s/coll-of ::author))

(s/def ::cache (s/map-of ::string ::int))

(s/def ::optional-value (s/keys :opt-un [::value ::type]))

(s/def ::contents (s/coll-of ::optional-value))
(s/def ::description ::optional-value)

(s/def ::date (s/nilable inst?))
(s/def ::published-date ::date)
(s/def ::updated-date ::date)

(s/def ::item (s/keys :req-un [::authors ::title ::link ::contents ::description]
                      :opt-un [::published-date ::updated-date]))

(s/def ::items (s/coll-of ::item))
(s/def ::entries ::items)
(s/def ::unflattened-items (s/coll-of ::items))

(s/def ::parsed-feed (s/keys :req-un [::entries]))
(s/def ::parsed-feeds (s/map-of ::keyword (s/* ::parsed-feed)))

(s/def ::mime-message (partial instance? MimeMessage))
(s/def ::mime-messages (s/coll-of ::message))

(s/def ::mail-session (partial instance? Session))

(s/def ::urls (s/coll-of ::url))

(s/def ::from ::string)
(s/def ::to ::string)
(s/def ::subject ::string)
(s/def ::html ::string)
(s/def ::host ::string)
(s/def ::port ::int)
(s/def ::username ::string)
(s/def ::password ::string)

(s/def ::message (s/keys :req-un [::from ::to ::subject ::html]
                         :opt-un [::date]))


(s/def ::xml (s/map-of keyword? ::string))

(s/def ::imap-configuration (s/keys :req-un [::host ::port ::username ::password]
                                    :opt-un [::to ::from]))

(s/def ::exit ::int)
(s/def ::err ::string)
(s/def ::out ::string)

(s/def ::shell-result (s/keys :req-un [::exit]
                              :opt-un [::err ::out]))


(defn url-gen []
  (gen/elements ["http://blog.gonzih.me/index.xml"]))

(s/def ::url
  (s/spec string? :gen url-gen))

(s/def ::folder-of-urls  (s/map-of ::keyword ::urls))
(s/def ::folder-of-items (s/map-of ::keyword ::items))
(s/def ::folder-of-unflattened-items (s/map-of ::keyword ::unflattened-items))
