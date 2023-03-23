(ns git.switch-branch
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(let [{:keys [out exit]}
      (-> (p/process "list-branches")
          (p/process ["fzf" "--preview=git log -b {} --pretty=format:'%h %d %s'"]
                     {:err :inherit
                      :out :string})
          deref)]
  (when-not (zero? exit)
    (System/exit exit))
  (p/shell "git" "switch" (str/trim out)))
