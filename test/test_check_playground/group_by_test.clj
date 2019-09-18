(ns test-check-playground.group-by-test
  (:require [test-check-playground.group-by :as sut]
            [clojure.test :refer [deftest is]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]))

(deftest group-by-nil
  (is (= {} (sut/group-by (fn [_]) nil))))

;; (group-by f coll)
;; Properties
;; X 1. It doesn't lose values.
;; X 2. All values are under the right key.
;; X 3. Duplicates
;; X 4. function returns nil
;; X 5. order

(defspec group-by-all-values
  (prop/for-all [mp (gen/map gen/nat (gen/elements [:a :b :c]))]
    (let [numbers (concat (keys mp) (keys mp) [:x :y])
          out (sut/group-by mp numbers)
          vals (mapcat val out)]
      (= (frequencies numbers)
         (frequencies vals)))))

(defspec group-by-right-keys
  (prop/for-all [mp (gen/map gen/nat (gen/elements [:a :b :c]))]
    (let [numbers (concat (keys mp) (keys mp) [:x :y])
          out (sut/group-by mp numbers)]
      (every? identity
              (for [[k vs] out
                    v vs]
                (= k (mp v)))))))

(defspec group-by-right-keys
  (prop/for-all [mp (gen/map gen/nat (gen/elements [:a :b :c]))]
    (let [numbers (concat (keys mp) (keys mp) [:x :y])
          out (sut/group-by mp numbers)]
      (every? identity
              (for [[k vs] out]
                (= vs (filter #(= k (mp %)) numbers)))))))


(comment

  (gen/sample (gen/map gen/nat (gen/elements [:a :b :c])))
  (group-by even? [1 2 3 4 5 6 7])

  (mapcat val {:a [1 2 3] :b [4 5 6]})
  
  )
