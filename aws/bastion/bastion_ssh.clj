(ns aws.bastion.bastion-ssh
  (:require [babashka.process :as p]
            [clojure.string :as str]
            [edamame.core :as eda]))

(defn run [cmd]
  (let [res (p/sh cmd)]
    (when (> (:exit res) 0)
      (->> res :err str/trim println)
      (System/exit (:exit res)))
    (->> res :out str/trim)))

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
