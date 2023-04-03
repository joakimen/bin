(ns aws.aws-vault.select-aws-profiles
  "select one or more aws profiles, filtered through fzf"
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn list-aws-profiles []
  (let [{:keys [exit err out]} (p/sh "list-aws-profiles")]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (str/trim out)))

(defn fzf [s]
  (let [{:keys [exit out]} @(p/process ["fzf" "-m"]
                                       {:in s :err :inherit
                                        :out :string})]
    (when-not (zero? exit)
      (System/exit exit))
    (str/trim out)))

(->> (list-aws-profiles)
     (fzf)
     (println))
