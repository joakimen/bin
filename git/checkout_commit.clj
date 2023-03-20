(ns git.checkout-commit
  "checkout a given revision using fzf"
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(let [res (p/sh {:err :inherit :out :string} "select-commit")]
  (when (> (:exit res) 0)
    (System/exit (:exit res)))
  (let [revision (-> res :out str/trim)]
    (println "checking out revision:" revision)
    (p/shell "git" "checkout" revision)))
