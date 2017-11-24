(ns feeds2imap.feeds-test
  (:require [feeds2imap.feeds :refer :all]
            [feeds2imap.test-helpers :refer [spec-fn]]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]
            [clojure.test :refer [deftest is]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.clojure-test :refer [defspec]]))

(deftest testing-specs-no-sideffects
  (doseq [fname (disj (stest/enumerate-namespace 'feeds2imap.feeds)
                      `parse `fetch `items->emails `new-items)]
    (is (spec-fn fname))))

(deftest testing-specs-with-sideffects
  (let [parse-memo (memoize feeds2imap.feeds/parse)]
    (with-redefs [feeds2imap.feeds/parse parse-memo]
      (doseq [fname [`parse `parse `new-items]]
        (is (spec-fn fname))))))

(deftest reduce-new-items-test
  (with-redefs [digest/md5 identity]
    (is (= (:new-items (reduce-new-items {"a" 1 "b" 2}
                                         {:b [{:uri "c"}]
                                          :a [{:uri "b"} {:uri "z"}]}))
           {:b [{:uri "c"}] :a [{:uri "z"}]}))
    (is (= (set (keys (:cache (reduce-new-items {"a" 1 "b" 2}
                                                {:b [{:uri "c"} {:uri "d"}]
                                                 :a [{:uri "b"} {:uri "z"}]}))))
           #{"a" "b" "c" "d" "z"}))
    (let [{:keys [cache new-items]}
          (reduce-new-items {} {:b [{:uri "http://b.com"} {:uri "https://b.com"}]
                                :a [{:uri "http://a.com"} {:uri "https://a.com"}]})]
      (is (= 2 (count cache)))
      (is (= 1 (count (:a new-items))))
      (is (= 1 (count (:b new-items)))))

    (is (new? #{"https://abc"} {:uri "https://cba"}))
    (is (not (new? #{"https://abc"} {:uri "https://abc"})))))

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

(defspec new?-spec
  1000
  (prop/for-all [cache (gen/map gen/string-alpha-numeric gen/pos-int)
                 item gen/string-alpha-numeric]
                (= (new? cache item)
                   (not (contains? cache (md5-identifier item))))))
