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
