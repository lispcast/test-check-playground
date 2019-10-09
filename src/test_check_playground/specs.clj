(ns test-check-playground.specs
  (:require [clojure.spec.alpha :as s]))

(defn ranged-rand  ;; BROKEN!
  "Returns random int in range start <= rand < end"
  [start end]
  (+ start (long (rand (- start end)))))
