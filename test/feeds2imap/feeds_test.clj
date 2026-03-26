(ns feeds2imap.feeds-test
  (:require [feeds2imap.feeds :refer :all]
            [feeds2imap.test-helpers :refer [spec-fn]]
            [feeds2imap.db :as db]
            [feeds2imap.logging]
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
                      `parse `fetch `new-items)]
    (is (spec-fn fname))))

(deftest testing-specs-with-sideffects
  (let [fetch-memo (memoize feeds2imap.feeds/fetch)]
    (with-redefs [feeds2imap.feeds/fetch fetch-memo
                  feeds2imap.logging/enabled? false]
      (doseq [fname [`parse `new-items]]
        (is (spec-fn fname))))))

(deftest filter-new-items-test
  (with-redefs [digest/md5 identity]
    (is (= (filter-new-items [{:uri "c" :folder "c"}
                              {:uri "b" :folder "b"}
                              {:uri "z" :folder "z"}])
           [{:uri "c" :folder "c"}
            {:uri "b" :folder "b"}
            {:uri "z" :folder "z"}]))))

(deftest update-cache-duplicate-guids-test
  (let [suffix (str (System/currentTimeMillis))
        dup-id (str "test-dup-" suffix)
        rep-id (str "test-rep-" suffix)
        col-id (str "test-col-" suffix)]
    ;; Duplicate guids in the same batch must not fail or prevent caching
    (db/update-cache! [dup-id dup-id])
    (is (not (db/is-new? dup-id)))
    ;; Calling update-cache! twice with the same guid must not throw
    (db/update-cache! [rep-id])
    (db/update-cache! [rep-id])
    (is (not (db/is-new? rep-id)))
    ;; Items whose identifiers collide must not reappear after first delivery
    (with-redefs [digest/md5 identity]
      (let [item-a {:uri col-id :folder :test}
            item-b {:uri col-id :folder :test}]
        ;; Both appear new before caching
        (is (= (filter-new-items [item-a item-b]) [item-a item-b]))
        ;; Cache the guids exactly as pull does
        (db/update-cache! (map feeds2imap.feeds/md5-identifier [item-a item-b]))
        ;; Neither should appear new after caching
        (is (empty? (filter-new-items [item-a item-b])))))))

(deftest uniq-identifier-test
  (is (= "uri" (uniq-identifier {:uri "uri" :url "url" :link "link"})))
  (is (= "url" (uniq-identifier {:uri nil :url "url" :link "link"})))
  (is (= "link" (uniq-identifier {:uri nil :url nil :link "link"})))
  (let [item {:uri nil :url nil :link nil :authors [{:name "authors"}]}]
    (is (= "authors" (uniq-identifier item))))
  (is (= "https://a.com" (uniq-identifier {:uri "http://a.com"}))))
