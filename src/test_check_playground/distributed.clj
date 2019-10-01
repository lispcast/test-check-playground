(ns test-check-playground.distributed
  (:refer-clojure :exclude [sync get])
  (:require [clj-http.client :as http]
            [cheshire.core :as json])
  (:import [java.util UUID]))

(declare sync)


(defn sync-store [store]
  {:storeid store
   :upstream []
   :waiting []})

(defn update-some [v k f & args]
  (if (contains? v k)
    (apply update v k f args)
    v))

(defn uuid []
  (str (UUID/randomUUID)))

(defn rdcr [kv [op id oid arg1 arg2]]
  (case op
    (:put "put")
    (let [m (clojure.core/get kv arg1)
          kv (cond
               (or (nil? m)
                   (nil? (:value m))
                   (= (:id m) oid)
                   (= (:value m)
                      (get-in kv [::ops-by-id oid :value])))
               (assoc kv
                      arg1
                      (assoc m
                             :value arg2
                             :id    id
                             :key arg1))

               (= (:value m) arg2)
               (assoc kv arg1
                      (assoc m
                             :value arg2
                             :id    id
                             :key   arg1))

               :else
               (-> kv
                   (assoc-in [arg1 :conflicts id]
                             {:id    id
                              :key   arg1
                              :value arg2})
                   (update-in [arg1 :conflicts] dissoc oid)))]
      (update kv ::ops-by-id assoc id {:id id
                                       :key arg1
                                       :oid oid
                                       :value arg2}))

    (:del "del")
    (let [m (clojure.core/get kv arg1)]
      (cond
        (nil? m)
        kv

        (= (:id m) oid)
        (assoc kv
               arg1 (assoc m
                           :value nil
                           :id id
                           :key arg1))

        :else
        kv))))

(defn store-get [store k]
  (let [upstream (reduce rdcr {}       (:upstream store))
        waiting  (reduce rdcr upstream (:waiting store))]
    (clojure.core/get waiting k)))




(defn store-put [store k v]
  (let [rec (store-get store k)
        oid (:id rec)]
    (update-some store :waiting conj [:put (uuid) oid k v])))

(defn store-del [store k]
  (let [rec (store-get store k)
        oid (:value rec)]
    (update-some store :waiting conj [:del (uuid) oid k])))


(defn create-store []
  (-> (http/post "http://localhost:8989/kv")
      :body
      (json/parse-string keyword)
      :kv-id))

(defn remote-sync [id cursor ops]
  (-> (str "http://localhost:8989/kv/" id)
      (http/post {:body (json/generate-string
                         {:cursor cursor
                          :ops ops})})
      :body
      (json/parse-string keyword)
      :ops))

(defn store-sync-down [store upstream]
  (let [ids-old (into #{} (map second (:upstream store)))
        ids-new (into ids-old (map second upstream))]
    (-> store
        (update :upstream into (remove #(contains? ids-old (second %)) upstream))
        (assoc :waiting (filterv #(not (contains? ids-new (second %))) (:waiting store))))))



(defn thread-runner [stores]
  (Thread/sleep 100)
  (doseq [id (keys @stores)
          :when (string? id)]
    ;;(println "syncing:" id)
    (try
      (sync stores id)
      (catch Throwable t
        (println t)))))

(defn run-thread [stores]
  (doto (Thread. (fn []
                   (if (:finished? @stores)
                     (println "Stopping thread")
                     (do
                       (thread-runner stores)
                       (recur)))))
    (.start))
  stores)

;; public

(defn create-stores []
  (let [s (atom {})]
    (run-thread s)
    s))

(defn new-store [stores]
  (let [id (create-store)
        store (sync-store id)]
    (swap! stores assoc id store)
    id))

(defn connect-store [stores id]
  (let [store (sync-store id)]
    (swap! stores (fn [stores]
                    (if (contains? stores id)
                      stores
                      (assoc stores id store))))
    nil))

(defn disconnect-store [stores id]
  (swap! stores dissoc id)
  nil)


(defn get [stores id key]
  (-> @stores
      (clojure.core/get id)
      (store-get key)
      :value))

(defn conflicts [stores id key]

  (-> @stores
      (clojure.core/get id)
      (store-get key)
      :conflicts
      vals
      (->> (map :value))
      set))

(defn put [stores id key value]
  (swap! stores update-some id store-put key value)
  nil)


(defn del [stores id key]
  (swap! stores update-some id store-del key)
  nil)

(defn delete [stores id]
  (disconnect-store stores id)
  (http/delete (str "http://localhost:8989/kv/" id))
  nil)

(defn shutdown-syncing [stores]
  (swap! stores assoc :finished? true)
  nil)

(defn sync [stores id]
  (let [store (clojure.core/get @stores id)
        sync (:waiting store)
        ops (remote-sync id (count (:upstream store)) sync)]
    (swap! stores update id store-sync-down ops))
  nil)

(comment

  (def strs (create-stores))


  (def s (new-store strs))
  (put strs s "a" 1)
  (put strs s "b" 4)
  (prn (get strs s "b"))
  (prn (conflicts strs s "b"))
  (sync strs s)
  (disconnect-store strs s)
  (connect-store strs s)
  (clojure.pprint/pprint (clojure.core/get @strs s))
  )
