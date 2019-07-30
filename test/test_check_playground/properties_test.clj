(ns test-check-playground.properties-test
  (:require [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [clojure.set :as set]))

;; Algebraic properties

;; simple formulas
;; conditional/flexible
;; functionality

;; inverse

(defspec inc-dec-inverse 100
  (prop/for-all [number gen/large-integer]
    (= number (inc (dec number)))))

(defspec dec-incc-inverse 100
  (prop/for-all [number gen/large-integer]
    (= number (dec (inc number)))))

(defspec reader-inverse 100
  (prop/for-all [value (gen/recursive-gen gen/vector gen/string-ascii)]
    (= value
       (read-string (pr-str value)))))

;; idempotence

(defspec set-conj-idempotent 100
  (prop/for-all [s (gen/set gen/large-integer)
                 number gen/large-integer]
    (= (conj s number)
       (-> s
           (conj number)
           (conj number)))))

;; commutativity

(defspec +-commutative 100
  (prop/for-all [n1 gen/large-integer
                 n2 gen/large-integer]
    (= (+ n1 n2)
       (+ n2 n1))))

;; associativity

;; x * y * z
;; (x * y) * z
;; x * (y * z)

(defspec *-associative 100
  (prop/for-all [x gen/small-integer
                 y gen/small-integer
                 z gen/small-integer]
    (= (* (* x y) z)
       (* x (* y z)))))

(defspec concat-associative 100
  (prop/for-all [x (gen/vector gen/small-integer)
                 y (gen/vector gen/small-integer)
                 z (gen/vector gen/small-integer)]
    (= (concat (concat x y) z)
       (concat x (concat y z)))))

(defspec merge-associative 100
  (prop/for-all [x (gen/map gen/keyword (gen/choose 0 9))
                 y (gen/map gen/keyword (gen/choose 0 9))
                 z (gen/map gen/keyword (gen/choose 0 9))]
    (= (merge (merge x y) z)
       (merge x (merge y z)))))

;; identity

(defspec concat-identity 100
  (prop/for-all [x (gen/vector gen/small-integer)]
    (= x
       (concat x [])
       (concat [] x))))

;; zero

(defspec *-zero 100
  (prop/for-all [x gen/large-integer]
    (= 0
       (* 0 x)
       (* x 0))))

(defn cross-product [l1 l2]
  (for [x l1
        y l2]
    [x y]))

(defspec cross-product-zero 100
  (prop/for-all [l (gen/vector gen/small-integer)]
    (= []
       (cross-product [] l)
       (cross-product l []))))

;; log format -- timestamps

;; merge in general is not commutative

(defspec merge-commutative-if 100
  (prop/for-all [n1 (gen/map gen/keyword (gen/choose 0 9))
                 n2 (gen/map gen/keyword (gen/choose 0 9))]
    (if (empty? (set/intersection (set (keys n1))
                                  (set (keys n2))))
      (= (merge n1 n2)
         (merge n2 n1))
      true)))

(defspec merge-commutative 100
  (prop/for-all [n1 (gen/map gen/keyword (gen/choose 0 9))
                 n2 (gen/map gen/keyword (gen/choose 0 9))]
    (= (set (keys (merge n1 n2)))
       (set (keys (merge n2 n1))))))

(comment
  (gen/sample gen/any-printable)

  (merge {:H 0} {:H 1})
  (merge {:H 1} {:H 0})
  )
