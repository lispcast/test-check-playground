(ns test-check-playground.stateful-test
  (:require [test-check-playground.stateful :as sut]
            [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]))

;; Testing stateful systems

;; 2 property strategies
;; Invariants
;; Model

;; Score counter

;; increment -- no negative numbers -- monotonically increasing
;; current-value

[:add 10]
[:val]

(def gen-add (gen/fmap #(vector :add %) gen/small-integer))
(def gen-val (gen/return [:val]))
(def gen-pause (gen/fmap #(vector :pause %) (gen/choose 0 10)))
(def gen-score-op (gen/frequency [[1 gen-pause]
                                  [1 gen-val]
                                  [9 gen-add]]))

(defn apply-score-op [counter [op arg]]
  (case op
    :pause (Thread/sleep arg)
    :val (sut/counter-value counter)
    :add (sut/increment counter arg)))

(defspec score-monotonically-increasing 100
  (prop/for-all [ops (gen/vector gen-score-op)]
    (let [counter (sut/new-counter)]
      (loop [ops ops]
        (if (empty? ops)
          true
          (let [s (sut/counter-value counter)]
            (try
              (apply-score-op counter (first ops))
              (catch Throwable t))
            (if (>= (sut/counter-value counter) s)
              (recur (rest ops))
              false)))))))

(defn apply-score-model-op [model [op arg]]
  (case op
    :pause model
    :val model
    :add (if (neg? arg)
           model
           (+ model arg))))

(defspec score-model-test 100
  (prop/for-all [ops (gen/vector gen-score-op)]
    (let [counter (sut/new-counter)
          model (reduce apply-score-model-op 0 ops)]
      (doseq [op ops]
        (try
          (apply-score-op counter op)
          (catch Throwable t)))
      (= model (sut/counter-value counter)))))

(comment

  (gen/sample gen-score-op)

  )

;; Key-value store

;; get
;; put
;; del
;; clr

(defn gen-get [key-gen]
  (gen/tuple (gen/return :get) key-gen))

(defn gen-put [key-gen val-gen]
  (gen/tuple (gen/return :put) key-gen val-gen))

(defn gen-del [key-gen]
  (gen/tuple (gen/return :del) key-gen))

(def gen-clr
  (gen/tuple (gen/return :clr)))

(defn gen-kv-op [key-gen val-gen]
  (gen/one-of [(gen-get key-gen)
               (gen-put key-gen val-gen)
               (gen-del key-gen)
               gen-clr]))

(defn apply-kv-op [store [op k v]]
  (case op
    :get (sut/kv-get store k)
    :put (sut/kv-put store k v)
    :del (sut/kv-del store k)
    :clr (sut/kv-clr store))
  nil)

{:kv {}
 :dels #{}}

(defn apply-kv-model-op [model [op k v]]
  (case op
    :get model
    :put (-> model
             (assoc-in [:kv k] v)
             (update :dels (fnil disj #{}) k))
    :del (-> model
             (update :kv (fnil dissoc {}) k)
             (update :dels (fnil conj #{}) k))
    :clr (-> model
             (assoc :kv {})
             (update :dels (fnil into #{}) (keys (:kv model))))))

;; encourage collisions

(comment

  (apply-kv-model-op {} [:put "a" 1])

  (apply-kv-model-op {} [:del "a"])

  (apply-kv-model-op {:kv {:a 1}} [:clr])

  )


(defspec kv-store-model 100
  (prop/for-all [ops (gen/vector (gen-kv-op (gen/frequency 
                                             [[10 (gen/elements ["a"
                                                                 "b"
                                                                 "c"
                                                                 "d"])]
                                              [1 gen/string-ascii]])
                                            gen/string-ascii))]
    (let [store (sut/new-kv-store)
          model (reduce apply-kv-model-op {} ops)]
      (doseq [op ops]
        (try
          (apply-kv-op store op)
          (catch Throwable t)))
      (and
       (every? (fn [[k v]]
                 (= v (sut/kv-get store k)))
               (:kv model))
       (every? (fn [k]
                 (nil? (sut/kv-get store k)))
               (:dels model))))))

(comment

  (gen/sample (gen-kv-op gen/string-ascii gen/string-ascii))


  )

;; Parallel

;; Challenge: reliably induce failures due to race conditions

;; Techniques:
;;  1. Running threads
;;  2. Relax comparisions--if our real system is equivalent to some possible ordering in our model

(defn children [{:keys [sequence threads]}]
  (for [[k [v & thread]] threads]
    {:sequence (conj sequence v)
     :threads (if (not (empty? thread))
                (assoc threads k thread)
                (dissoc threads k))}))

(defn branch? [x]
  (-> x :threads empty? not))

(defn possible-interleavings [sequences]
  (let [threads (into {} (map vector (range) (remove empty? sequences)))]
    (->> (tree-seq branch? children {:sequence [] :threads threads})
         (remove branch?)
         (map :sequence)
         distinct)))

(comment

  (children {:sequence [:beginning] :threads {0 [:a :b :c]}})

  (possible-interleavings [[:a :b] [:x :y]])

  (possible-interleavings [[] [] [] [] []])

  (count (possible-interleavings [(range 10) (map #(+ 1000 %) (range 10))]))
  
  )

(defn run-score-counter-threads [counter ops-sequences]
  (let [latch (promise)
        dones (doall (for [ops ops-sequences]
                       (let [done (promise)]
                         (.start
                          (Thread. (fn []
                                     @latch
                                     (doseq [op ops]
                                       (try
                                         (apply-score-op counter op)
                                         (catch Throwable t)))
                                     (deliver done :done))))
                         done)))]
    (deliver latch :go!)
    (run! deref dones)))

(defspec score-model-parallel-test 100
  (prop/for-all [ops-sequences (gen/vector (gen/vector gen-score-op 0 5) 2)]
    (let [poss (possible-interleavings ops-sequences)
          models (map #(reduce apply-score-model-op 0 %) poss)]
      (every? identity
              (for [_ (range 50)]
                (let [counter (sut/new-counter)]
                  (run-score-counter-threads counter ops-sequences)
                  (let [counter-val (sut/counter-value counter)]
                    (some #(= counter-val %) models))))))))

(comment

  (gen/sample (gen/vector (gen/vector gen-score-op 0 5) 2))

  )

(def gen-kv-op-collision (gen-kv-op (gen/frequency
                                     [[10 (gen/elements ["a"
                                                         "b"
                                                         "c"
                                                         "d"])]
                                      [1 gen/string-ascii]])
                                    gen/string-ascii))

(defn run-kv-store-threads [store ops-sequences]
  (let [latch (promise)
        dones (doall (for [ops ops-sequences]
                       (let [done (promise)]
                         (.start
                          (Thread. (fn []
                                     @latch
                                     (doseq [op ops]
                                       (try
                                         (apply-kv-op store op)
                                         (catch Throwable t)))
                                     (deliver done :done))))
                         done)))]
    (deliver latch :go!)
    (run! deref dones)))

(defn kv-store-model-equiv? [store model]
  (and
   (every? (fn [[k v]]
             (= v (sut/kv-get store k)))
           (:kv model))
   (every? (fn [k]
             (nil? (sut/kv-get store k)))
           (:dels model))))

(defspec kv-store-model-parallel 100
  (prop/for-all [init-sequence (gen/vector gen-kv-op-collision)
                 ops-sequences (gen/vector (gen/vector gen-kv-op-collision 0 7) 2)]
    (let [poss (possible-interleavings ops-sequences)
          poss (map #(concat init-sequence %) poss)
          models (map #(reduce apply-kv-model-op {} %) poss)]
      (every? identity
              (for [_ (range 50)]
                (let [store (sut/new-kv-store)]
                  (doseq [op init-sequence]
                    (try
                      (apply-kv-op store op)
                      (catch Throwable t)))
                  (run-kv-store-threads store ops-sequences)
                  (some #(kv-store-model-equiv? store %) models)))))))

(comment

  (gen/sample (gen/vector (gen/vector gen-kv-op-collision 0 5) 2))

  )
