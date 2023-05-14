(ns git.on-feature-branch
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(let [cur-branch (-> (p/sh "git branch --show-current") :out str/trim)
      base-branches ["main" "master" "develop"]
      is-base-branch (some #(= cur-branch %) base-branches)]
  (when is-base-branch
    (throw (ex-info nil {:babashka/exit 1}))))
