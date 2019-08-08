(ns test-check-playground.size
  (:require [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]))

;; What is size?

;; measure of range from which values are randomly chosen

;; numbers => smallest 0, bigger is bigger (further from 0 is bigger)
;; strings => smallest "", bigger is longer string
;; collections => smallest is empty, bigger has more elements
;; recursive => smallest is empty/unnested, bigger is bigger and deeply nested

;; return => no size
;; choose => no size
;; elements => no size
;; one-of => no size

(def gen-size (gen/sized (fn [size] (gen/return size))))

(def gen-10-nat (gen/resize 10 gen/nat))

(def gen-fast-nat (gen/scale (fn [size] (* size size size)) gen/nat))

(comment

  (gen/sample (gen/tuple gen/nat gen-fast-nat gen-size))
  
  )
