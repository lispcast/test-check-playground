(ns test-check-playground.specs-test
  (:require [test-check-playground.specs :as sut]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [clojure.spec.alpha :as s]))

(comment

  (gen/sample (s/gen int?))
  (gen/sample (s/gen pos?))

  (gen/sample (s/gen ::sut/age-range2))

  (gen/sample (s/gen (s/inst-in #inst "1980" #inst "1990")))

  (gen/sample (s/gen ::sut/temp-in-new-orleans))

  (gen/sample (s/gen ::sut/names-with-A2))
  (gen/sample (s/gen ::sut/names-with-A3))

  (gen/sample (s/gen boolean?))

  (map (fn [n] (s/conform ::sut/names-with-A3 n))
       (gen/sample (s/gen ::sut/names-with-A3)))

  (s/exercise ::sut/person-tuple)

  )
