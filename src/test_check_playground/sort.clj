(ns test-check-playground.sort
  (:refer-clojure :exclude [merge]))

(defn merge [l1 l2]
  (lazy-seq
   (cond
     (empty? l1)
     l2

     (empty? l2)
     l1

     (< (first l1) (first l2))
     (cons (first l1) (merge (rest l1) l2))

     :else
     (cons (first l2) (merge l1 (rest l2))))))

(defn mergesort* [v]
  (case (count v)
    0 ()
    1 (seq v)

    ;; else
    (let [half (quot (count v) 2)]
      (merge (mergesort* (subvec v 0 half))
             (mergesort* (subvec v half))))))

(defn mergesort [ls]
  (mergesort* (vec ls)))

