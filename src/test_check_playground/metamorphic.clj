(ns test-check-playground.metamorphic
  (:require [cheshire.core :as json]
            [clj-http.client :as http]))

(defn search
  ([query]
   (search query 0))
  ([query offset]
   (let [res (-> "http://en.wikipedia.org/w/api.php"
                 (http/get {:query-params {:action "query"
                                           :format "json"
                                           :list "search"
                                           :srsearch query
                                           :sroffset offset}})
                 :body
                 (json/parse-string keyword))
         hits        (get-in res [:query :search])
         total-hits  (get-in res [:query :searchinfo :totalhits])
         next-offset (get-in res [:continue :sroffset])]
     total-hits)))

