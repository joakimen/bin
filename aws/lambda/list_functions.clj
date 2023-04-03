(ns aws.lambda.list-functions
  "navigate to a lambda in default browser"
  (:require [babashka.process :as p]
            [cheshire.core :as json]
            [clojure.string :as str]))

(defn run [cmd]
  (let [{:keys [exit out err]} (p/sh cmd)]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (str/trim out)))

(defn list-functions []
  (let [functions (run "aws lambda list-functions")]
    (:Functions (json/parse-string functions true))))

(->> (list-functions)
     (map :FunctionName)
     (sort)
     (mapv println))
