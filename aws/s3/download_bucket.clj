(ns aws.s3.download-bucket
  "downloads a bucket using 'aws s3 sync' and fzf-filtering"
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn select-buckets []
  (let [{:keys [out exit]} (p/sh {:err :inherit} "select-buckets")]
    (when-not (zero? exit)
      (System/exit exit))
    (->> out str/trim str/split-lines)))

(defn download-bucket [bucket]
  (let [{:keys [out err exit]} (p/sh "aws" "s3" "sync" bucket (str/replace-first bucket "s3://" "s3-"))]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit}))
      (->> out str/trim))))

(let [buckets (select-buckets)]
  (println (str "downloading buckets:\n" (str/join "\n" buckets)))
  (doall (pmap download-bucket buckets)))
