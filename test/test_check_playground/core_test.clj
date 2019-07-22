(ns test-check-playground.core-test
  (:require [test-check-playground.core :refer :all]
            [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]))

(str/upper-case "abcd")

(comment
 (defspec always-passes 100
   (prop/for-all []
     true))

 (defspec always-fails 100
   (prop/for-all []
     false)))

(comment
  (remove-ns 'test-check-playground.core-test))

(defspec length-doesnt-change
  (prop/for-all [s gen/string-ascii]
    (= (count s) (count (str/upper-case s)))))

(defspec everything-uppercased
  (prop/for-all [s gen/string-ascii]
    (every? #(if (Character/isLetter %)
               (Character/isUpperCase %)
               true)
            (str/upper-case s))))

(defspec idempotent
  (prop/for-all [s gen/string-ascii]
    (= (str/upper-case s)
       (str/upper-case (str/upper-case s)))))

(Character/isUpperCase \º)
(Character/isLetter \º)
(str/upper-case "º")
(str/upper-case "ß")

(gen/sample gen/string-ascii)

;; Numbers
;;; Integers
(gen/sample gen/nat)
(gen/sample gen/small-integer 100)
(gen/sample gen/large-integer 100)
(gen/sample (gen/large-integer* {:min 10 :max 10000}) 100)

(gen/sample (gen/choose 0 10000))


;;; Ratios
(gen/sample gen/ratio)
(gen/sample gen/big-ratio)

;;; Doubles
(gen/sample gen/double)
(gen/sample (gen/double* {:infinite? false :NaN? false}))

;;; BigInts
(gen/sample gen/size-bounded-bigint)

;; Characters and strings

;;; Characters
(gen/sample gen/char)
(gen/sample gen/char-ascii)
(gen/sample gen/char-alphanumeric)
(gen/sample gen/char-alpha)

;;; Strings
(gen/sample gen/string)
(gen/sample gen/string-ascii)
(gen/sample gen/string-alphanumeric)

;;; keywords
(gen/sample gen/keyword)
(gen/sample gen/keyword-ns)

;;; symbols
(gen/sample gen/symbol)
(gen/sample gen/symbol-ns)

;;; uuid
(gen/sample gen/uuid)

;;; boolean
(gen/sample gen/boolean)

;; Collections

;;; Vector
(gen/sample (gen/vector gen/nat 2 4))
(gen/sample (gen/vector-distinct gen/nat))

;;; List
(gen/sample (gen/list gen/boolean))
(gen/sample (gen/list-distinct gen/small-integer))

;;; Set
(gen/sample (gen/set gen/nat))
(gen/sample (gen/sorted-set gen/nat))

;; Map
(gen/sample (gen/map gen/keyword gen/string-ascii))

;; Tuple
(gen/sample (gen/tuple gen/nat gen/string-alphanumeric gen/boolean))

;; Entity
{:first-name "Eric"
 :last-name "Normand"
 :age 38}

(gen/sample (gen/hash-map :first-name gen/string-alphanumeric
                          :last-name gen/string-alphanumeric
                          :age gen/nat))

;; Not-empty
(gen/sample (gen/vector gen/boolean))
(gen/sample (gen/not-empty (gen/vector gen/boolean)))

;; example nesting
(gen/sample (gen/vector (gen/vector gen/boolean)))

;; recursive
(drop 90 (gen/sample (gen/recursive-gen gen/vector gen/boolean) 100))

;; Random selection
(gen/sample (gen/elements [:a :b :c :d :e]))
(gen/sample (gen/return 1))
(gen/sample (gen/shuffle [1 2 3 4 5 6 7]))
(gen/sample (gen/one-of [gen/string-alphanumeric
                         gen/nat
                         (gen/return nil)
                         (gen/vector gen/nat)]))
(gen/sample (gen/frequency [[10 gen/nat]
                            [1 (gen/return nil)]
                            [2 gen/string-alphanumeric]]) 100)
(gen/sample (gen/choose 0 100))

;; Building simple generators

;; such-that (like filter)

(gen/sample (gen/such-that even? gen/nat))
(gen/sample (gen/such-that odd? gen/nat))
(gen/sample (gen/such-that neg? gen/small-integer))
(gen/sample (gen/such-that #(zero? (mod % 100)) gen/nat))
(gen/sample (gen/such-that #(re-matches #"[abcd]+" %) gen/string))

;; fmap (like map)

(gen/sample (gen/fmap #(* 2 %) gen/nat))
(gen/sample (gen/fmap #(inc (* 2 %)) gen/nat))
(gen/sample (->> gen/nat
                 (gen/fmap inc)
                 (gen/fmap -)))
(gen/sample (gen/fmap #(* 100 %) gen/nat))
(gen/sample (gen/fmap #(apply str %) (gen/not-empty (gen/vector (gen/elements [\a \b \c \d])))))

;; bind

(gen/sample (gen/bind gen/nat #(gen/vector gen/nat %)))
(gen/sample (gen/bind (gen/tuple (gen/fmap inc gen/nat)
                                 (gen/fmap inc gen/nat))
                      (fn [[n m]]
                        (gen/vector
                         (gen/vector gen/small-integer m)
                         n))))

;; let

(gen/sample (gen/let [len gen/nat]
              (gen/vector gen/nat len)))
(gen/sample (gen/let [[n m] (gen/tuple (gen/fmap inc gen/nat)
                                       (gen/fmap inc gen/nat))]
              (gen/vector
               (gen/vector gen/small-integer m)
               n)))
