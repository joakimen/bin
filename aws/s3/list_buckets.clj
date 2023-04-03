(ns aws.s3.list-buckets
  (:require [babashka.process :as p]
            [cheshire.core :as json]
            [clojure.string :as str]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (str/trim out)))

(defn parse-json [s]
  (json/parse-string s true))

(->> (run "aws s3api list-buckets")
     (parse-json)
     :Buckets
     (map :Name)
     (map #(str "s3://" %))
     (mapv println))
