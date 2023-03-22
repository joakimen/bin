(ns git.revert-commit
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(let [{:keys [out exit]} (p/sh {:err :inherit :out :string} "select-commit")]

  (when-not (zero? exit)
    (System/exit 1)) ;; just quiet ctrl-c

  (when-not (string? out)
    (throw (ex-info (str "expected sha: " out) {:babashka/exit 1})))

  (let [sha (str/trim out)]
    (println "reverting revision:" sha)
    (p/shell {:continue true} "git" "revert" sha)))
