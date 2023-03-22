(ns git.checkout-commit
  "checkout a given revision using fzf"
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(let [{:keys [out exit]} (p/sh {:err :inherit :out :string} "select-commit")]

  (when-not (zero? exit)
    (System/exit exit))

  (when-not (string? out)
    (throw (ex-info (str "expected sha: " out) {:babashka/exit 1})))

  (let [sha (str/trim out)]
    (println "checking out revision:" sha)
    (p/shell "git" "checkout" sha)))
