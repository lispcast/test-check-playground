(ns test-check-playground.mutation-test
  (:require [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.string :as str]
            [test-check-playground.email :as email]))

;; Mutation testing
;;  1. Generate correct data
;;  2. Change it randomly
;;  3. Filter out ones that are still correct

(defn add-char [s char idx]
  (let [idx (mod idx (inc (count s)))]
    (str (.substring s 0 idx)
         char
         (.substring s idx))))

(defn drop-char [s idx]
  (if (empty? s)
    ""
    (let [idx (mod idx (count s))]
      (str (.substring s 0 idx)
           (.substring s (inc idx))))))

(def gen-add-char (gen/tuple (gen/return :add)
                             gen/char-ascii
                             gen/nat))

(def gen-drop-char (gen/tuple (gen/return :drop)
                              gen/nat))

(def gen-str-mutation (gen/one-of [;;gen-drop-char
                                   gen-add-char]))

(defn mutate [s [op & data]]
  (case op
    :add  (apply add-char  s data)
    :drop (apply drop-char s data)))

(def gen-bad-email (gen/such-that
                    (complement email/valid?)
                    (gen/fmap (fn [[e ms]]
                                (reduce mutate e ms))
                              (gen/tuple email/gen-email2
                                         (gen/vector gen-str-mutation)))))


(def gen-non-email-strings  (gen/such-that (complement email/valid?) gen/string-ascii))

(defspec bad-emails-fail 100
  (prop/for-all [s gen-bad-email]
    (try
      (email/save-email! s)
      false
      (catch Throwable t
        true))))

(comment

  (gen/sample (gen/such-that (complement email/valid?) gen/string-ascii))

  (add-char "" \J 12)
  (drop-char "" 4)

  (gen/sample gen-add-char)
  (gen/sample (gen/vector gen-str-mutation))
  (reduce mutate "abc" [[:add \J 12] [:drop 1]])
  (gen/sample email/gen-email)
  (gen/sample gen-bad-email)

  (gen/sample email/gen-email2)

  )
