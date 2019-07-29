(ns test-check-playground.email
  (:require [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check :as tc]
            [clojure.string :as str]))

(def email-re
  #"(([^<>()\[\]\.,;:\s@\"]+(\.[^<>()\[\]\.,;:\s@\"]+)*)|(\".+\"))@(([^<>()\[\]\.,;:\s@\"]+\.)+[^<>()\[\]\.,;:\s@\"]{2,})")

;; this won't work
(gen/sample (gen/such-that #(re-matches email-re %) gen/string-ascii))

(def gen-email-char (gen/such-that #(re-matches #"[^<>()\[\]\.,;:\s@\"]" (str %))
                                   gen/char-ascii))

(def gen-email-string (gen/not-empty (gen/fmap str/join (gen/vector gen-email-char))))

(def gen-regular-email-name (gen/fmap
                             (fn [[n1 nn]]
                               (str/join "." (cons n1 nn)))
                             (gen/tuple gen-email-string
                                        (gen/vector gen-email-string))))
(def gen-irregular-email-name (gen/fmap
                               #(str \" % \")
                               (gen/not-empty gen/string-ascii)))

(def gen-email-name (gen/frequency [[10 gen-regular-email-name]
                                    [1 gen-irregular-email-name]]))

(def gen-email-domain (gen/fmap
                       (fn [[hns tld1 tld2]]
                         ;; hns is vector, so add at the end
                        (str/join "." (conj hns (str tld1 tld2))))
                       (gen/tuple (gen/not-empty (gen/vector gen-email-string))
                                  gen-email-string
                                  gen-email-string)))

(def gen-email (gen/fmap
                (fn [[name domain]]
                  (str name
                       "@"
                       domain))
                (gen/tuple gen-email-name
                           gen-email-domain)))

(def gen-email-name2 (gen/elements ["bob"
                                    "suzy"
                                    "john"
                                    "jill"]))
(def gen-email-domain2 (gen/elements ["gmail.com"
                                      "hotmail.com"
                                      "example.com"
                                      "yahoo.com"]))

(def gen-char-digit (gen/elements (vec "0123456789"))) 

(def gen-email2 (gen/fmap
                 (fn [[name n domain]]
                   (str name (str/join n) "@" domain))
                 (gen/tuple gen-email-name2
                            (gen/vector gen-char-digit 0 4)
                            gen-email-domain2)))

(comment
  (gen/sample gen-email-char)
  (gen/sample gen-email-string)
  (gen/sample gen-email2)

  (tc/quick-check 100
                  (prop/for-all [email gen-email2]
                    (re-matches email-re email)))


  )



(comment

  (re-matches email-re "eric@lispcast.com")
  (re-matches email-re "a@dfsfs.cm")

  )
