(ns test-check-playground.shrinkage
  (:require [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [clojure.test.check :as tc]
            [clojure.string :as str]))

(tc/quick-check 100 (prop/for-all [v (gen/vector gen/nat)]
                      (not (contains? (set v) 17)))
                :reporter-fn (fn [res]
                               (when (= :shrink-step (:type res))
                                 (prn (get-in res [:shrinking :args])
                                      (get-in res [:shrinking :smallest])))))

