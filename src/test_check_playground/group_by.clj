(ns test-check-playground.group-by
  (:refer-clojure :exclude [group-by]))

(defn group-by [f coll]
  (reduce (fn [mp e]
            (update mp (f e) (fnil conj []) e))
          {} coll))

(comment

  (let [mp {}]
   (group-by mp (concat (keys mp) (keys mp) [:x :y])))

  )
