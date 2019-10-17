(ns test-check-playground.fn-specs-test
  (:require [test-check-playground.fn-specs :as sut]
            [clojure.test :refer [deftest is]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]))

(comment

  (stest/instrument `sut/my-sort)
  (stest/unstrument `sut/my-sort)

  (s/exercise-fn `sut/my-sort)

  (prn (stest/check `sut/my-sort))

  (gen/sample (s/gen (s/coll-of (s/with-gen any?
                                  #(s/gen int?)))))

  (sut/my-sort 1)

  (gen/sample (s/gen ::sut/sorted-seq))
  )

(deftest my-sort-test
  (let [res (stest/check `sut/my-sort)]
    (is (true? (-> res
                   first
                   :clojure.spec.test.check/ret
                   :pass?))
        (stest/abbrev-result (first res)))))

(defspec my-sort-idem 100
  (prop/for-all [coll (s/gen ::sut/homogeneous-coll)]
    (let [res (sut/my-sort coll)]
      (= res (sut/my-sort res)))))
