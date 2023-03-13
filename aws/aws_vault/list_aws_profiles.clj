(ns aws.aws-vault.list-aws-profiles
  (:require [babashka.fs :as fs]))

(let [conf (fs/read-all-lines (str (fs/path (fs/home) ".aws" "config")))
      pat #"\[profile ([a-zA-Z0-9\-]+)"]

  (->> conf
       (filter #(re-find pat %))
       (map #(second (re-find pat %)))
       (mapv println)
 ;; there's probably a less knuckle-headed way of doing this.. 
       ))
