(ns test-check-playground.output-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [clojure.edn :as edn]))

;; Typical way to test a function
;; 1. Generate the input
;; 2. Run our function (with input)
;; 3. Test the output

edn/read-string
pr-str

;; Generate the output to test a function
;; 1. Generate the output
;; 2. Convert the output to an input
;; 3. Run the function (on input)
;; 4. Compare output to generated output

(defspec test-read-string 100
  (prop/for-all [output gen/any-printable-equatable]
    (let [input (pr-str output)]
      (= (edn/read-string input) output))))

(comment

  (drop 50 (gen/sample gen/any-printable-equatable 200))

  )

(def gen-char-no-newlines (gen/such-that #(not= % \newline) gen/char-ascii))
(def gen-str-no-newlines  (gen/fmap str/join (gen/vector gen-char-no-newlines)))w


(defn lines [s]
  )

(defspec test-lines 100
  (prop/for-all [output (gen/not-empty (gen/vector gen-str-no-newlines))]
    (let [input (str/join "\n" output)]
      (= output (lines input)))))

(comment

  (gen/sample gen-char-no-newlines)
  (gen/sample gen-str-no-newlines)

  (str/join "\n" ["" ""])
  (lines "\n")
  )

(def gen-char-no-whitespace (gen/such-that #(not (Character/isWhitespace %))
                                           gen/char-ascii))
(def gen-str-no-whitespace  (gen/fmap str/join (gen/vector gen-char-no-whitespace)))

(defn words [s]
  (str/split s #"\s+"))

(defspec test-words 100
  (prop/for-all [output (gen/not-empty (gen/vector gen-str-no-whitespace))]
    (let [input (str/join " " output)]
      (= output (words input)))))

;; video example

[{:moving? true
  :frames 3}
 {:moving? false
  :frames 10}
 {:moving? true
  :frames 45} ]
