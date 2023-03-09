(ns aws.lambda.list-functions
  "navigate to a lambda in default browser"
  (:require [babashka.process :as p]
            [cheshire.core :as json]
            [clojure.string :as str]))

(defn run [cmd]
  (let [res (p/sh cmd)]
    (when (> (:exit res) 0)
      (->> res :err str/trim println)
      (System/exit (:exit res)))
    (->> res :out str/trim)))

(defn list-functions []
  (let [functions (run "aws lambda list-functions")]
    (->> (json/parse-string functions true)
         :Functions)))

(->> (list-functions)
     (map :FunctionName)
     (sort)
     (mapv println))
