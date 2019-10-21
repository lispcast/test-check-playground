(ns test-check-playground.todo-mvc-test
  (:require [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [etaoin.api :as w]
            [etaoin.keys :as k]
            [clojure.string :as str]))

(defonce driver (w/chrome))

(defn load-todo-app [driver]
  (w/go driver "http://todomvc.com/examples/react/#/")
  (w/js-execute driver "window.localStorage.clear();")
  (w/reload driver)
  (w/wait-visible driver [{:class :new-todo}]))

(defn new-todo [driver name]
  (w/fill driver {:tag :input :class :new-todo} name)
  (w/fill driver {:tag :input :class :new-todo} k/enter))

(defn todos [driver]
  (try
    (w/query-all driver [{:tag :ul :class :todo-list} {:tag :li}])
    (catch Exception e
      ())))

(defn todo-texts [driver]
  (map #(w/get-element-text-el driver %) (todos driver)))

(defn delete-top-todo [driver]
  (w/mouse-move-to driver [{:tag :ul :class :todo-list} {:tag :li}])
  (w/click driver {:tag :button :class :destroy}))

(defspec create-and-delete 10
  (prop/for-all [todo-names (gen/not-empty
                             (gen/vector
                              (gen/not-empty (gen/fmap str/trim gen/string-ascii))))]
    (load-todo-app driver)
    (if-not (empty? (todos driver))
      (throw (ex-info "Todos should be empty." {}))
      (do
        (doseq [t todo-names]
          (new-todo driver t))
        (if-not (= (todo-texts driver) todo-names)
          false
          (do
            (doseq [_ todo-names]
              (delete-top-todo driver))
            (empty? (todos driver))))))))

(comment
  (load-todo-app driver)
  (new-todo driver "do taxes")
  (todos driver)
  (todo-texts driver)
  (delete-top-todo driver)
  )
