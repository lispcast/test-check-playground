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

;; Invariants
;; what doesn't change?

(defspec sort-always-list 100
  (prop/for-all [numbers (gen/vector gen/large-integer)]
    (seq? (s/mergesort numbers))))

(defspec sort-same-length 100
  (prop/for-all [numbers (gen/vector gen/large-integer)]
    (= (count (s/mergesort numbers))
       (count numbers))))

(defspec sort-same-elements 100
  (prop/for-all [numbers (gen/vector gen/large-integer)]
    (= (frequencies numbers)
       (frequencies (s/mergesort numbers)))))

;; idempotence

(defspec sort-idempotent 100
  (prop/for-all [number (gen/vector gen/large-integer)]
    (= (s/mergesort number)
       (-> number
           s/mergesort
           s/mergesort))))

;; commutativity of elements

(defspec sort-commutative 100
  (prop/for-all [n1 (gen/shuffle (range 100))
                 n2 (gen/shuffle (range 100))]
    (= (s/mergesort n1)
       (s/mergesort n2))))

(defspec sort-identity 100
  (prop/for-all [n1 (gen/vector gen/large-integer)]
    (= Long/MIN_VALUE
       (-> n1
           (conj Long/MIN_VALUE)
           s/mergesort
           first))))

(comment
  (sort [])
  (s/mergesort [])
  (gen/sample (gen/vector (gen/choose 0 4)))
  (frequencies [:a :b :a :c :d :d :d])
  )

