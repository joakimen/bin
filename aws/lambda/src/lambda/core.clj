(ns lambda.core
  (:require [lambda.lib.lambda :as lambda]))

(defn list-functions []
  (let [client (lambda/client)]
    (->> (lambda/list-functions client)
         (map :FunctionName)
         (sort)
         (run! println))))
