(ns test-check-playground.specs
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as sgen]
            [clojure.string :as str]))

(s/def ::age-range (s/and
                    int?
                    (fn [a] (<= 18 a 120))))

(s/def ::age-range2 (s/int-in 18 121))

(s/def ::birth-year (s/inst-in #inst "1980" #inst "1990"))

(s/def ::temp-in-new-orleans (s/double-in :min -11.7 :max 38.9 :NaN? false :infinite? false))

(s/def ::names-with-A (s/and
                       string?
                       (fn [n] (str/starts-with? n "A"))))

(s/def ::names-with-A2 (s/with-gen
                         (s/and
                          string?
                          (fn [n] (str/starts-with? n "A")))
                         (fn []
                           (s/gen #{"Aaron" "Alice" "Amanda" "Allen"}))))

(s/def ::names-with-A3 (s/with-gen
                         (s/and
                          string?
                          (fn [n] (str/starts-with? n "A")))
                         (fn []
                           (sgen/fmap (fn [chars]
                                        (str "A" (str/lower-case (apply str chars))))
                                      (sgen/vector (sgen/char-alpha))))))

;; 1. Use the generator that spec builds for you.
;; 2. Steal the generator from another spec.
;; 3. Construct a generator using clojure.spec.gen.alpha
;; 4. Construct a generator using clojure.test.check.generators



(comment
  (s/valid? ::birth-year #inst "1985")
  (s/valid? ::temp-in-new-orleans 20.0)

  (s/valid? ::names-with-A2 "Albert")

  (str/starts-with? 1 "A")

  (s/conform (s/cat :year ::birth-year) [#inst "1975"])

  )

(s/def ::person-tuple (s/cat :name ::names-with-A3
                             :dob ::birth-year
                             :temp ::temp-in-new-orleans))

(comment

  (s/valid? ::age-range 50)
;; => true
  (s/valid? ::age-range 18)
  ;; => true
  (s/valid? ::age-range 10)
  ;; => false

  (s/valid? ::age-range "10")
;; => false


  (s/valid? pos? "aa")
  
  )
