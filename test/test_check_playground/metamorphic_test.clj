(ns test-check-playground.metamorphic-test
  (:require [test-check-playground.metamorphic :as wiki]
            [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]))


;; metamorphic properties

(def fruit-terms ["fruit" "apple" "banana" "orange" "grape" "tree" "ripe" "sweet"])

(defspec wikipedia-search-not-operator 10
  (prop/for-all [pos-word (gen/elements fruit-terms)
                 neg-word (gen/elements fruit-terms)]
    (let [pos-hits 0;; (wiki/search pos-word)
          neg-query (str pos-word " !" neg-word)
          neg-hits 0;; (wiki/search neg-query)
          ]
      (>= pos-hits neg-hits))))

;; gen input
;; run function
;; modify input
;; run function
;; compare the two results

;; adding negative query => less than or equal to results

;; image detection

(comment
 (defspec fog-detection 100
   (prop/for-all [image gen-image]
     (let [object (detect image)
           image-with-fog (add-fog image)
           object-with-fog (detec image)]
       (= object object-with-fog)))))

;; max

(defspec max-test 100
  (prop/for-all [numbers (gen/vector-distinct gen/small-integer {:min-elements 2
                                                                 :max-elements 100})]
    (let [m (apply max numbers)
          numbers' (disj (set numbers) m)
          m' (apply max numbers')]
      (> m m'))))

