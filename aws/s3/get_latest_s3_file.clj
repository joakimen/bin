(ns aws.s3.get-latest-s3-file
  (:require [babashka.process :as p]
            [cheshire.core :as json]
            [clojure.string :as str]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (if (zero? exit)
      (str/trim out) (throw (ex-info (str/trim err) {:babashka/exit exit})))))

(defn fzf [s]
  (let [{:keys [out exit]} @(p/process ["fzf"]
                                       {:in s :err :inherit
                                        :out :string})]
    (when-not (zero? exit)
      (System/exit exit))
    (str/trim out)))

(defn parse-json [s]
  (json/parse-string s true))

(defn list-buckets []
  (->> (run "aws s3api list-buckets") parse-json :Buckets (map :Name)))

(defn find-latest-file [bucket]
  (->> (p/pipeline
        (p/pb "aws" "s3" "ls" bucket "--recursive")
        (p/pb "sort")
        (p/pb "tail" "-n" "1")
        (p/pb "awk '{print $4}"))
       last :out slurp str/trim))

(defn download-file [bucket key]
  (run "aws" "s3" "cp" (format "s3://%s/%s" bucket key) "."))

(let [bucket (->> (list-buckets) (str/join "\n") fzf)
      key (-> bucket (find-latest-file))]
  (when (str/blank? key)
    (throw (ex-info (str "No file found in bucket: " bucket) {:babashka/exit 1})))

  (println "bucket:" bucket)
  (println "key:" key)
  (println "downloading file...")
  (download-file bucket key)
  (println "done."))
