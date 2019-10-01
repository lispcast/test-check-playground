(ns test-check-playground.distributed-test
  (:require [test-check-playground.distributed :as sut]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [clojure.set :as set]
            [clojure.pprint :refer [pprint]]))

;; Eventually consistent

(defonce storesa (sut/create-stores))
(defonce storesb (sut/create-stores))

(defn gen-put [store-gen key-gen val-gen]
  (gen/tuple (gen/return :put)
             store-gen
             key-gen
             val-gen))

(def gen-sleep
  (gen/tuple (gen/return :sleep) (gen/choose 1 110)))

(defn gen-op [store-gen key-gen val-gen]
  (gen/frequency [[9 gen-sleep]
                  [9 (gen-put store-gen key-gen val-gen)]]))

(defn key->store [store]
  (case store :A storesa :B storesb))

(defn sys-op [s [op store k v]]
  (case op
    :sleep (Thread/sleep store)
    :put (sut/put (key->store store) s k v)))

(defn sys-same-kv? [s k]
  (and (= (sut/get storesa s k)
          (sut/get storesb s k))
       (= (sut/conflicts storesa s k)
          (sut/conflicts storesb s k))))

(defn sys-same-kvs? [s ks]
  (every? #(sys-same-kv? s %) ks))

(defn settle [s]
  (let [t0 (System/currentTimeMillis)]
   (loop []
     (cond
       (sys-same-kvs? s ["a" "b" "c"])
       true

       (> (- (System/currentTimeMillis) t0) 3000)
       false

       :else
       (do
         (Thread/sleep 100)
         (recur))))))

(defspec settles-within-reasonable-time 10
  (prop/for-all [ops (gen/vector (gen-put (gen/elements [:A :B])
                                          (gen/elements ["a" "b" "c"])
                                          gen/nat))]
    (let [s (sut/new-store storesa)]
      (sut/connect-store storesb s)
      (try
        (doseq [op ops]
          (sys-op s op))
        (settle s)
        (finally
          (sut/disconnect-store storesa s)
          (sut/disconnect-store storesb s))))))

(def empty-model {:S {} :A {} :B {}})

(defn model-reducer [model [op store k v :as operation]]
  (case op
    :sleep model
    :sync
    (let [S (reduce (fn [S [k v]]
                      (cond
                        (not (:unsynced v))
                        S

                        (= (get-in S [k :value])
                           (:value v))
                        S

                        (= (get-in S [k :value])
                           (:replacing v))
                        (assoc-in S [k :value] (:value v))

                        (get-in S [k])
                        (update-in S [k :conflicts] (fnil conj #{}) (:value v))

                        :else
                        (assoc-in S [k :value] (:value v))))
                    (:S model)
                    (get model store))]
      (-> model
          (assoc :S S store S)
          (vary-meta update-in [:history] (fnil conj []) operation)
          ))
    :put (-> model
             (assoc-in [store k :value] v)
             (assoc-in [store k :replacing] (get-in model [store k :value]))
             (assoc-in [store k :unsynced] true)
             (vary-meta update-in [:history] (fnil conj []) operation)
             )))

(defn combinations [xs ys]
  (for [x xs y ys] [x y]))

(defn consistent-kv? [s store model k]
  (and (= (sut/get (key->store store) s k)
          (get-in model [store k :value]))
       (= (sut/conflicts (key->store store) s k)
          (get-in model [store k :conflicts] #{}))))

(defn consistent? [s model]
  (every? (fn [[store k]]
            (consistent-kv? s store model k))
          (combinations [:A :B] ["a" "b" "c"])))

(defn sync-cycle [skip n]
  (take n (drop skip (cycle [[:sync :A] [:sync :B]]))))

(defn possible-ops [op]
  (set (for [skip-before [0 1]
             n-before (range 4)
             skip-after [0 1]
             n-after (range 4)]
         (concat (sync-cycle skip-before n-before)
                 [op]
                 (sync-cycle skip-after n-after)))))

(def full-sync-ops
  (set (for [skip [0 1]]
         (sync-cycle skip 3))))

(defn run-test
  ([ops]
   (run-test ops false))
  ([ops dev?]
   (let [s (sut/new-store storesa)]
     (sut/connect-store storesb s)
     (try
       (let [models (reduce (fn [models op]
                              (sys-op s op)
                              (let [new-models (set (for [model models
                                                          ops (possible-ops op)]
                                                      (reduce model-reducer model ops)))
                                    consistent-models (set (filter #(consistent? s %) new-models))]
                                (if (empty? consistent-models)
                                  (reduced #{})
                                  consistent-models)))
                            #{empty-model} ops)]
         (if (empty? models)
           (when dev?
             (println "No consistent models."))
           (do
             (settle s)
             (let [settled-models (set (for [model models
                                             ops full-sync-ops]
                                         (reduce model-reducer model ops)))]
               (when dev?
                 (println "Models")
                 (doseq [model settled-models]
                   (pprint model)
                   (pprint (:history (meta model))))
                 (doseq [store [:A :B]]
                   (println store (into {} (map (juxt identity
                                                      (juxt #(sut/get       (key->store store) s %)
                                                            #(sut/conflicts (key->store store) s %)))
                                                ["a" "b" "c"])))))
               (not-empty (filter #(consistent? s %) settled-models))))))
       (finally
         (sut/disconnect-store storesa s)
         (sut/disconnect-store storesb s))))))

(defspec distributed-client-model 10
  (prop/for-all [ops (gen/vector (gen-op (gen/elements [:A :B])
                                         (gen/elements ["a" "b" "c"])
                                         gen/nat)
                                 1 20)]
    (some (fn [_] (run-test ops)) (range 20))))

(comment
  (model-reducer empty-model [:put :A "a" 1])
  (run-test [[:put :B "b" 2]
             [:put :B "a" 0]
             [:put :B "b" 4]
             [:put :A "a" 4]
             [:put :A "b" 0]
             [:put :A "a" 1]
             [:put :A "b" 2]
             [:put :A "b" 4]
             [:sleep 61]]
            true)

  [[:put :A "c" 0] [:put :B "c" 0]]
  )
