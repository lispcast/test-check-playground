(ns test-check-playground.login-model-test
  (:require [test-check-playground.fn-specs :as sut]
            [clojure.test :refer [deftest is]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]))

;; Send emails about failed payments

;; Build model
;; Check invariants

(def gen-email-name (gen/elements ["bob"
                                   "suzy"
                                   "john"
                                   "jill"]))
(def gen-email-domain (gen/elements ["gmail.com"
                                     "hotmail.com"
                                     "example.com"
                                     "yahoo.com"]))

(def gen-address (gen/fmap
                  (fn [[name domain]]
                    (str name "@" domain))
                  (gen/tuple gen-email-name
                             gen-email-domain)))

(def gen-time (gen/fmap #(java.util.Date. (* 1000000 %)) gen/small-integer))

(defn mk-email [address time]
  {:from "eric@purelyfunctional.tv"
      :to address
      :subject "Payment failed"
      :time time
      :body (str "Hello!
Your payment failed at " (str time) ". Please login and check your details.

Thanks!
Eric")})

(def gen-email
  (gen/fmap
   (fn [[address time]]
     (mk-email address time))
   (gen/tuple gen-address gen-time)))

;; these two functions need analogues in the real system

(defn sent-recently? [model email]
  (when-some [last-sent (get-in model [:sent (:to email)])]
    (<= (- (-> email :time .getTime)
           last-sent)
        hours-24)))

(defn update-sent [model email]
  (assoc-in model [:sent (:to email)] (-> email :time .getTime)))

(defn send-email-model [model email]
  (if (sent-recently? model email)
    model
    (-> model
        (update-sent email)
        (update (:to email) (fnil conj []) email))))

(defn run-model [model emails]
  (reduce send-email-model model emails))

(defspec every-gets-their-email
  (prop/for-all [emails (gen/fmap #(vec (sort-by :time %)) (gen/vector gen-email))]
    (let [model (run-model {} emails)]
      (every? true?
              (for [email emails]
                (not (empty? (get model (:to email)))))))))

(def hours-24 (* 1000 60 60 24))

(defspec nobody-gets-2-within-24-hours
  (prop/for-all [emails (gen/fmap #(vec (sort-by :time %)) (gen/vector gen-email))]
    (let [model (run-model {} emails)]
      (every? true?
              (for [[address inbox] model
                    :when (string? address)
                    e1 inbox
                    e2 inbox
                    :when (not= e1 e2)]
                (let [t1 (-> e1 :time .getTime)
                      t2 (-> e2 :time .getTime)]
                  (> (Math/abs (- t1 t2))
                     hours-24)))))))

(defspec people-get-2-if-far-apart
  (prop/for-all [emails (->> (gen/vector gen-email)
                             (gen/fmap #(vec (concat %
                                                     (map
                                                      (fn [{:keys [to time]}]
                                                        (mk-email to (java.util.Date.
                                                                      (+ hours-24 hours-24
                                                                         (.getTime time)))))
                                                      %))))
                             (gen/fmap #(vec (sort-by :time %)))
)]
    (let [model (run-model {} emails)]
      (every? true?
              (for [[address inbox] model
                    :when (string? address)]
                (>= (count inbox) 2))))))


(comment

  (def m (run-model {} [{:from "eric@purelyfunctional.tv",
                         :to "bob@gmail.com",
                         :subject "Payment failed",
                         :time #inst "1970-01-01T00:00:00.000-00:00",
                         :body
                         "Hello!\nYour payment failed at Wed Dec 31 18:00:00 CST 1969. Please login and check your details.\n\nThanks!\nEric"}
                        {:from "eric@purelyfunctional.tv",
                         :to "bob@gmail.com",
                         :subject "Payment failed",
                         :time #inst "1970-01-03T00:00:00.000-00:00",
                         :body
                         "Hello!\nYour payment failed at Fri Jan 02 18:00:00 CST 1970. Please login and check your details.\n\nThanks!\nEric"}]))

  (.getYear #inst "1970-01-01T00:00:00.000-00:00")
  (mod (.getTime #inst "1970-01-01T00:00:10.000-00:00") hours-24)

  (clojure.pprint/pprint m)
  )
