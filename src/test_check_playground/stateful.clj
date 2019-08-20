(ns test-check-playground.stateful)

;; score counter

(defn new-counter []
  (atom 0))

(defn increment [counter diff]
  (assert (not (neg? diff)))
  (swap! counter + diff)
  nil)

(defn counter-value [counter]
  @counter)


;; key-value store

(defn new-kv-store []
  (atom {}))

(defn kv-get [store key]
  (get @store key))

(defn kv-put [store key value]
  (swap! store assoc key value)
  nil)

(defn kv-del [store key]
  (swap! store dissoc key)
  nil)

(defn kv-clr [store]
  (swap! store empty)
  nil)
