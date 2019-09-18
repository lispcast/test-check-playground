(ns test-check-playground.frequencies-test
  (:require [clojure.test :refer [deftest is]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [clojure.string :as str]))

(defn my-frequencies [coll]
  (reduce (fn [m e]
            (update m e (fnil inc 0)))
          {} coll))

;; when to test: after implementation

;; adversarial
;; can't duplicate implementation

;; functionality
(defspec frequencies-returns-map
  (prop/for-all [coll (gen/vector gen/nat)]
    (map? (frequencies coll))))
(defspec frequencies-vals-pos
  (prop/for-all [coll (gen/vector gen/nat)]
    (every? pos? (vals (frequencies coll)))))
(defspec frequencies-keys-from-coll
  (prop/for-all [coll (gen/vector gen/nat)]
    (= (set (keys (frequencies coll)))
       (set coll))))

(defn unfreq [m]
  (for [[v n] m
        _ (range n)]
    v))

;; properties
(defspec frequencies-inverse
  (prop/for-all [coll (gen/vector gen/nat)]
    (let [out (frequencies coll)
          input (unfreq out)]
      (= (sort coll) (sort input)))))

(defspec frequencies-commutative
  (let [ls (unfreq {:a 10 :b 10 :c 10})]
    (prop/for-all [a1 (gen/shuffle ls)
                   a2 (gen/shuffle ls)]
      (= (frequencies a1) (frequencies a2)))))

(defspec frequencies-metamorphic
  (let [s "here is my test"]
    (prop/for-all [coll (gen/vector (gen/one-of [(gen/return s)
                                                 (gen/fmap
                                                  (fn [i] (str/join [(.substring s 0 i)
                                                                     (.substring s (inc i))]))
                                                  (gen/choose 0 (dec (count s))))])) ]
      (= (set (keys (frequencies coll)))
         (set coll)))))

;; generate output
(defspec frequencies-gen-output
  (prop/for-all [output (gen/map gen/keyword (gen/fmap inc gen/nat))]
    (= output (frequencies (unfreq output)))))
(defspec frequencies-gen-output-order
  (let [output {:a 10 :b 10 :c 10}
        input (unfreq output)]
    (prop/for-all [input (gen/shuffle input)]
      (= output (frequencies input)))))


(comment

  (gen/sample (gen/map gen/keyword (gen/fmap inc gen/nat)))
  
  (let [s "here is my test"]
    (gen/sample (gen/vector (gen/one-of [(gen/return s)
                                         (gen/fmap
                                          (fn [i] (str/join [(.substring s 0 i)
                                                             (.substring s (inc i))]))
                                          (gen/choose 0 (dec (count s))))]))))

  (let [s "here is my test"]
    (str/join [(.substring s 0 (dec (count s)))
               (.substring s (inc (dec (count s))))]))

  (for [[v n] {:a 1 :b 2 :c 3}
        _ (range n)]
    v)

  
 (frequencies [:a :b :c :a :a :b]))
