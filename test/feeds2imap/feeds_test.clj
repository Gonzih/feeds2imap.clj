(ns feeds2imap.feeds-test
  (:require [feeds2imap.feeds :refer :all]
            [feeds2imap.test-helpers :refer [spec-fn]]
            [feeds2imap.db :as db]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.test :refer [deftest is use-fixtures]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]))

(defn db-fixture [f]
  (db/init-db!)
  (f))

(use-fixtures :once db-fixture)

(deftest testing-specs-no-sideffects
  (doseq [fname (disj (stest/enumerate-namespace 'feeds2imap.feeds)
                      `parse `fetch `items->emails `new-items)]
    (is (spec-fn fname))))

(deftest testing-specs-with-sideffects
  (let [parse-memo (memoize feeds2imap.feeds/parse)]
    (with-redefs [feeds2imap.feeds/parse parse-memo]
      (doseq [fname [`parse `parse `new-items]]
        (is (spec-fn fname))))))

(deftest filter-new-items-test
  (with-redefs [digest/md5 identity]
    (is (= (filter-new-items {:b [{:uri "c"}]
                              :a [{:uri "b"} {:uri "z"}]})
           {:b [{:uri "c"}]
            :a [{:uri "b"} {:uri "z"}]}))))

(deftest pmap-test
  (let [run (partial into {})]
    (let [urls {:a [{:uri "a"} {:uri "b"}]
                :b [{:uri "c"} {:uri "d"}]}
          map-result {:a ["a" "b"]
                      :b ["c" "d"]}]
      (is (= (run (map-items :uri urls)) map-result))
      (is (= (run (pmap-items :uri urls)))) map-result)

    (let [urls {:a [{:uri "a"} {:uri nil}]
                :b [{:uri nil} {:uri "d"}]}
          filter-result {:a [{:uri "a"}]
                         :b [{:uri "d"}]}]
      (is (= (run (filter-items :uri urls)) filter-result)))

    (let [urls {:a [[{:uri "a"}] [{:uri "b"}]]
                :b [[{:uri "c"} {:uri "d"}]]}
          flatten-result {:a [{:uri "a"} {:uri "b"}]
                          :b [{:uri "c"} {:uri "d"}]}]
      (is (= (run (flatten-items urls)) flatten-result)))))

(deftest uniq-identifier-test
  (is (= "uri" (uniq-identifier {:uri "uri" :url "url" :link "link"})))
  (is (= "url" (uniq-identifier {:uri nil :url "url" :link "link"})))
  (is (= "link" (uniq-identifier {:uri nil :url nil :link "link"})))
  (let [item {:uri nil :url nil :link nil :authors [{:name "authors"}]}]
    (is (= "authors" (uniq-identifier item))))
  (is (= "https://a.com" (uniq-identifier {:uri "http://a.com"}))))
