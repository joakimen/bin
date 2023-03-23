(ns git.revert-commit
  (:require [babashka.process :as p]))

(let [{:keys [out exit]}
      (-> (p/process "list-commits")
          (p/process ["fzf"] {:err :inherit
                              :out :string})
          deref)]
  (when-not (zero? exit)
    (System/exit exit))
  (p/shell {:continue true} "git" "revert" (re-find #"^[a-f0-9]+" out)))
