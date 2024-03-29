(defproject test-check-playground "0.1.0-SNAPSHOT"
  :description "Code to accompany Property-Based Testing with test.check, part of PurelyFunctional.tv"
  :url "https://purelyfunctional.tv/courses/property-based-testing-with-test-check/"
  :license {:name "CC0 1.0 Universal (CC0 1.0) Public Domain Dedication"
            :url "http://creativecommons.org/publicdomain/zero/1.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [cheshire "5.9.0"]
                 [clj-http "3.10.0"]]
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.10.0"]
                                  [etaoin "0.3.5"]]}})
