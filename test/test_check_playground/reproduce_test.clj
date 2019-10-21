(ns test-check-playground.reproduce-test
  (:require [test-check-playground.reproduce :as sut]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]))

(def gen-create
  (gen/tuple (gen/return :create)))

(defn gen-put [gen-key]
  (gen/tuple (gen/return :put)
             gen-key
             (gen/not-empty gen/string)))

(defn gen-get [gen-key]
  (gen/tuple (gen/return :get)
             gen-key))

(def gen-destroy
  (gen/tuple (gen/return :destroy)))

(defn gen-op [gen-key]
  (gen/frequency [[1 gen-create]
                  [10 (gen-put gen-key)]
                  [20 (gen-get gen-key)]
                  [1 gen-destroy]
                  ]))

(defn run-sys [id name [op arg1 arg2]]
  (case op
    :create  (sut/create name)
    :put     (sut/put id arg1 arg2)
    :get     (sut/get id arg1)
    :destroy (sut/destroy id)))



