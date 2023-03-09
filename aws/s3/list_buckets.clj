(ns aws.s3.list-buckets
  (:require [babashka.process :as p]
            [cheshire.core :as json]
            [clojure.string :as str]))

(defn run [cmd]
  (let [res (p/sh cmd)]
    (when (> (:exit res) 0)
      (->> res :err str/trim println)
      (System/exit (:exit res)))
    (->> res :out str/trim)))

(defn parse-json [s]
  (json/parse-string s true))

(defn list-buckets []
  (let [res (run "aws s3api list-buckets")]
    (->> (parse-json res)
         :Buckets
         (map :Name)
         (map #(str "s3://" %)))))

(doall (map println (list-buckets)))
