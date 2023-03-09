(ns aws.s3.download-bucket
  "downloads a bucket using 'aws s3 sync' and fzf-filtering"
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn select-buckets []
  (let [res (p/sh {:err :inherit} "select-buckets")]
    (when (> (:exit res) 0)
      (System/exit (:exit res)))
    (->> res :out str/trim str/split-lines)))

(defn download-bucket [bucket]
  (let [res (p/sh "aws" "s3" "sync" bucket (str/replace-first bucket "s3://" "s3-"))]
    (when (> (:exit res) 0)
      (println (:err res))
      (System/exit (:exit res)))
    (->> res :out str/trim)))

(let [buckets (select-buckets)]
  (println (str "downloading buckets:\n" (str/join "\n" buckets)))
  (doall (pmap download-bucket buckets)))
