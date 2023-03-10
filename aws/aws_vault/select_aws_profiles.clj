(ns aws.aws-vault.select-aws-profiles
  "select one or more aws profiles, filtered through fzf"
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn list-aws-profiles []
  (let [res (p/sh "list-aws-profiles")]
    (when (> (:exit res) 0)
      (some->> res :err str/trim println)
      (System/exit (:exit res)))
    (->> res :out str/trim)))

(defn fzf [s]
  (let [res @(p/process ["fzf" "-m"]
                        {:in s :err :inherit
                         :out :string})]
    (when (> (:exit res) 0)
      (System/exit (:exit res)))
    (->> res :out str/trim)))

(->> (list-aws-profiles)
     (fzf)
     (println))
