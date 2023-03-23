(ns git.list-branches
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(let [{:keys [out err exit]} (p/sh "git for-each-ref --format='%(refname:short)' refs/heads")]
  (when-not (zero? exit)
    (throw (ex-info (str/trim err) {:babashka/exit exit})))
  (-> out str/trim println))
