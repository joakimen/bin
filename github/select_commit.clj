(ns github.select-commit
  "select a single commit, filtered by fzf"
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(let [res (-> (p/process "git log --pretty=format:'%Cred%h%Creset -%C(yellow)%d%Creset %s %Cgreen(%cr) %C(bold blue)<%an>%Creset' --abbrev-commit")
              (p/process {:err :inherit :out :string} "fzf")
              deref)]
  (when (> (:exit res) 0)
    (System/exit (:exit res)))
  (println (re-find #"^[a-f0-9]+" (-> res :out str/trim))))
