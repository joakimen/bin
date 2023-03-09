#!/usr/bin/env bb
(ns aws.s3.select-bucket
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn fzf [s]
  (let [res @(p/process ["fzf" "-m"]
                        {:in s :err :inherit
                         :out :string})]
    (when (> (:exit res) 0)
      (System/exit (:exit res)))
    (->> res :out str/trim)))

(defn run [cmd]
  (let [res (p/sh cmd)]
    (when (> (:exit res) 0)
      (->> res :err str/trim println)
      (System/exit (:exit res)))
    (->> res :out str/trim)))

(->> (run "list-buckets")
     (fzf)
     (println))

(comment
  (->> (run "list-buckets")
       (fzf)
       (println))
 ;; 
  )
