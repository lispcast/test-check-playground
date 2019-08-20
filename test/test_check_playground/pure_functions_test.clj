(ns test-check-playground.pure-functions-test
  (:require [test-check-playground.pure-functions :as sut]
            [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]))

;; Testing pure functions

;; Main challenge: Cover the entire desired behavior

;; Main technique: adversarial

;; reverse

(defspec reverse-inverse-of-self 100
  (prop/for-all [ls (gen/vector gen/nat)]
    (= ls (sut/reverse (sut/reverse ls)))))

(defspec reverse-recursive 100
  (prop/for-all [l1 (gen/vector gen/nat)
                 l2 (gen/vector gen/nat)]
    (= (concat
        (sut/reverse l2)
        (sut/reverse l1))
       (sut/reverse (concat l1 l2)))))

;; sum
;; distinguish it from product

;; avoid duplicating code in the test


(defspec sum-list-of-constants
  (prop/for-all [constant gen/small-integer
                 count gen/nat]
    (let [ls (repeat count constant)]
      (= (* constant count)
         (sut/sum ls)))))

(defspec sum-list-recursive
  (prop/for-all [l1 (gen/vector gen/small-integer)
                 l2 (gen/vector gen/small-integer)]
    (= (sut/sum (concat l1 l2))
       (sut/sum (concat l2 l1))
       (sut/sum [(sut/sum l1)
                 (sut/sum l2)]))))

(defspec sum-list-commutative
  (prop/for-all [ls (gen/vector gen/small-integer)]
    (let [ls' (reverse ls)]
      (= (sut/sum ls')
         (sut/sum ls)))))

;; min

(defspec min-recursive
  (prop/for-all [ls (gen/not-empty (gen/vector gen/small-integer))]
    (let [m (sut/min ls)]
      (every? #(<= m %) ls))))

(deftest min-empty-list
  (is (thrown? Throwable (sut/min []))))

;; video-id



(defn ->video [id]
  (assoc-in {} [:metadata
                :connections
                :comments
                :uri]
            (str "https://video.com/videos/" id "/more-stuff")))

(defspec video-id-extraction 100
  (prop/for-all [id gen/nat]
    (let [id' (str id)
          v (->video id)]
      (= id' (sut/video-id v)))))

;; strip-query

(def gen-url (->> (gen/vector (gen/not-empty gen/string-alphanumeric))
                  (gen/fmap #(str/join "/" %))
                  (gen/fmap #(str "https://example.com/" %))))

(def gen-qs (->> (gen/map (gen/not-empty gen/string-alphanumeric)
                          (gen/not-empty gen/string-alphanumeric))
                 (gen/fmap #(map (fn [[k v]] (str k "=" v)) %))
                 (gen/fmap #(str/join "&" %))
                 (gen/fmap #(str "?" %))))

(defspec url-with-query 100
  (prop/for-all [url gen-url
                 qs  gen-qs]
    (= url
       (sut/strip-query (str url qs)))))

(defspec url-without-query 100
  (prop/for-all [url gen-url]
    (= url (sut/strip-query url))))

(defspec url-idempotent 100
  (prop/for-all [url gen-url
                 qs gen-qs]
    (let [uri (str url qs)]
      (= (sut/strip-query uri)
         (sut/strip-query (sut/strip-query uri))))))

(comment

  (sut/min [])

  (gen/sample gen-video)

  (gen/sample gen-qs)

  )
