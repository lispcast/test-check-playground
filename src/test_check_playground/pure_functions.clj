(ns test-check-playground.pure-functions
  (:refer-clojure :exclude [reverse min])
  (:require [clojure.string :as str]))

(defn reverse [ls]
  (into () ls))

(defn sum [nums]
  (reduce + 0 nums))

(defn min [nums]
  (assert (not (empty? nums)))
  (first (sort nums)))

(defn video-id [video]
  (-> video
      :metadata
      :connections
      :comments
      :uri
      (->> (re-find #"videos/(\d+)"))
      second))

(defn strip-query [url]
  (str/replace url #"[?].*" ""))
