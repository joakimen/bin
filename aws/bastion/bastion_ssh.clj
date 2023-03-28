#!/usr/bin/env bb
(ns aws.bastion.bastion-ssh
  (:require [babashka.process :as p]
            [clojure.string :as str]
            [edamame.core :as eda]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (str/trim out)))

(defn list-bastion-instances []
  (let [instances (run "list-bastion-instances")]
    (eda/parse-string instances)))

(defn fzf [s]
  (let [res @(p/process ["fzf" "-m"]
                        {:in s :err :inherit
                         :out :string})]
    (when (> (:exit res) 0)
      (System/exit (:exit res)))
    (->> res :out str/trim)))

(defn ssm-connect [instance]
  (let [instanceid (:instanceid instance)]
    (println "connecting to instance:" instanceid)
    (p/shell "aws" "ssm" "start-session" "--target" instanceid)))

(let [instance (->>
                (list-bastion-instances)
                (map str)
                (str/join "\n")
                (fzf)
                (eda/parse-string))]
  (ssm-connect instance))

(comment
  (def i (list-bastion-instances))

  ;;
  )
