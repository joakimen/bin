(ns git.select-commit
  "select a single commit, filtered by fzf"
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn run [& args]
  (let [res (p/sh args)
        err (:err res)
        exit (:exit res)]
    (when (not (zero? exit))
      (println (cond (string? err) (str/trim err)))
      (System/exit exit))
    (->> res :out str/trim)))

(defn fzf [s]
  (let [res @(p/process ["fzf"]
                        {:in s :err :inherit
                         :out :string})]
    (when (> (:exit res) 0)
      (System/exit (:exit res)))
    (->> res :out str/trim)))

(defn list-commits []
  (run "git" "log" "--pretty=format:%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset" "--abbrev-commit"))

(let [commits (list-commits)
      commit (fzf commits)
      sha (re-find #"^[a-f0-9]+" commit)]
  (println sha))
