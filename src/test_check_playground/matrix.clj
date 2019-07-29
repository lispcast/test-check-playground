(ns test-check-playground.matrix
  (:require  [clojure.test.check.generators :as gen]))

(defn gen-matrix [gen]
  (gen/let [[n m] (gen/tuple (gen/fmap inc gen/nat)
                             (gen/fmap inc gen/nat))]
    (gen/vector
     (gen/vector gen m)
     n)))

(defn gen-matrix2 [gen]
  (gen/fmap
   (fn [[r c ns]]
     (mapv vec (take r (partition c (cycle ns)))))
   (gen/tuple (gen/fmap inc gen/nat)
              (gen/fmap inc gen/nat)
              (gen/not-empty (gen/vector gen 1000)))))

(defn gen-elements [gen]
  (gen/fmap cycle (gen/vector gen 10)))

(defn gen-directive [gen]
  (gen/tuple (gen/elements [:row :col])
             (gen-elements gen)))

(defn matrix-fold [matrix [rc? elements]]
  (cond
    (empty? matrix)
    [[(first elements)]]

    (= :row rc?)
    (let [cols (count (first matrix))]
      (conj matrix (vec (take cols elements))))

    (= :col rc?)
    (let [rows (count matrix)]
      (mapv conj matrix elements))))

(defn gen-matrix3 [gen]
  (gen/fmap
   (fn [directives]
     (reduce matrix-fold [] directives))
   (gen/not-empty (gen/vector (gen-directive gen)))))

(comment

  (gen/sample (gen-matrix  gen/small-integer))
  (gen/sample (gen-matrix2 gen/small-integer))
  (gen/sample (gen-matrix3 gen/small-integer))

  (gen/sample (gen-directive gen/small-integer))

  (matrix-fold [] [:row [0 1 2 3]])

  (matrix-fold [[0 0 0]] [:row [1 2 3 4 5 6]])

  (matrix-fold [[0 0 0]
                [1 1 1]] [:col [1 2 3 4 5 6]])

  )

