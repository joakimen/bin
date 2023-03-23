(ns git.checkout-commit
  "checkout a given revision using fzf"
  (:require [babashka.process :as p]))

(let [{:keys [out exit]}
      @(-> (p/process "list-commits")
           (p/process ["fzf"] {:err :inherit
                               :out :string}))]
  (when-not (zero? exit)
    (System/exit exit))
  (p/shell {:continue true} "git" "checkout" (re-find #"^[a-f0-9]+" out)))
