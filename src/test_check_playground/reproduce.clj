(ns test-check-playground.reproduce
  (:refer-clojure :exclude [get])
  (:require [clojure.string :as str]))

(def dbs (atom {:next-id 0}))

(defn map-keys [mp f]
  (into {}
    (for [[k v] mp]
      (if (string? k)
        [k (f v)]
        [k v]))))

(defn create [db-name]
  (let [new-dbs (swap! dbs #(if-some [[id] (first (filter (fn [[_ {n :name}]]
                                                            (= db-name n))
                                                          (:dbs %)))]
                              %
                              (-> %
                                  (assoc-in [:dbs (:next-id %) :name] db-name)
                                  (update :next-id inc))))]
    (first (first (filter (fn [[_ {n :name}]]
                            (= db-name n))
                          (:dbs new-dbs))))))

(defn put [db-id k v]
  (if (contains? (:dbs @dbs) db-id)
    (swap! dbs assoc-in [:dbs db-id k] v)
    (throw (ex-info "Database not found." {:db-id db-id})))
  nil)

(defn get [db-id k]
  (if (contains? (:dbs @dbs) db-id)
    (let [v (get-in @dbs [:dbs db-id k])
          n (get-in @dbs [:dbs db-id :name])]
      (if (and v
               (str/index-of n \c)
               (str/index-of k \l)
               (str/index-of v \j))
        (throw (NullPointerException.))
        v))
    (throw (ex-info "Database not found." {:db-id db-id}))))

(defn destroy [db-id]
  (if (contains? (:dbs @dbs) db-id)
    (swap! dbs update :dbs dissoc db-id)
    (throw (ex-info "Database not found." {:db-id db-id})))
  nil)

(defn reset-dbs []
  (reset! dbs {:next-id 0})
  nil)
