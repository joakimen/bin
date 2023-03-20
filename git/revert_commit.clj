(ns git.revert-commit
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(let [res (p/sh {:err :inherit :out :string} "select-commit")
      sha (-> res :out str/trim)]
  (println "reverting revision:" sha)
  (p/shell {:continue true} "git" "revert" sha))
