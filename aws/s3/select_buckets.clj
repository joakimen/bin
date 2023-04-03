#!/usr/bin/env bb
(ns aws.s3.select-buckets
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn fzf [s]
  (let [{:keys [exit out]} @(p/process ["fzf" "-m"]
                                       {:in s :err :inherit
                                        :out :string})]
    (when-not (zero? exit)
      (System/exit exit))
    (str/trim out)))

(defn run [cmd]
  (let [{:keys [exit err out]} (p/sh cmd)]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (str/trim out)))

(->> (run "list-buckets")
     (fzf)
     (println))

(comment
  (->> (run "list-buckets")
       (fzf)
       (println))
 ;; 
  )
