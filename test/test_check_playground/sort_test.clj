(ns test-check-playground.sort-test
  (:require [test-check-playground.sort :as s]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]))

;; Testing functionality
;; Ensuring it does what it is supposed to

;; you can't reimplement

;; Comparing with a model

(defspec sort-with-model 100
  (prop/for-all [numbers (gen/vector gen/large-integer)]
    (= (sort numbers) (s/mergesort numbers))))

(distinct [1 2 3 3 3 3 2])

(defspec distinct-with-model 100
  (prop/for-all [numbers (gen/vector (gen/choose 0 4))]
    (= (count (set numbers))
       (count (distinct numbers)))))

;; think about the meaning

(defspec first-element-smaller 100
  (prop/for-all [numbers (gen/not-empty (gen/vector gen/large-integer))]
    (let [s (s/mergesort numbers)
          f (first s)]
      (every? #(<= f %) numbers))))

(defspec elements-in-order 100
  (prop/for-all [numbers (gen/not-empty (gen/vector gen/large-integer))]
    (let [s (s/mergesort numbers)]
      (apply <= s))))


(comment
  (sort [])
  (s/mergesort [])
  (gen/sample (gen/vector (gen/choose 0 4)))
  )

