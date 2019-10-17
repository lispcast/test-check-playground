(ns test-check-playground.fn-specs
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]))

(defn sorted-coll? [s]
  (not (pos? (reduce
              (fn [r [a b]]
                (if (pos? r)
                  (reduce r)
                  (compare a b)))
              -1
              (partition 2 1 s)))))

(sorted-coll? [2 3 4])
(compare 1 0)

(defn comparable? [x]
  (instance? Comparable x))

(s/def ::sorted-seq (s/and (s/with-gen seq?
                             #(gen/fmap (fn [c]
                                          (or (seq c) ()))
                                        (s/gen (s/coll-of int?))))
                           sorted-coll?))

(s/def ::homogeneous-coll (s/or :ints (s/coll-of int?)
                                :strings (s/coll-of string?)
                                :chars (s/coll-of char?)
                                :keywords (s/coll-of keyword?)))

(defn comparable-coll? [coll]
  (try
   (doseq [x coll
           y coll]
     (compare x y))
   true
   (catch ClassCastException e
     false)))

(comment

  (comparable-coll? ["a" :a])

  )

(s/fdef my-sort
  :args (s/cat :coll (s/with-gen comparable-coll?
                       #(s/gen ::homogeneous-coll)))
  :ret ::sorted-seq
  :fn #(= (frequencies (-> % :args :coll))
          (frequencies (-> % :ret))))

(defn my-sort [coll]
  (sort coll))
