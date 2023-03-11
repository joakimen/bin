(ns file.select-project
  "select a project, filtered through fzf
   cd'ing to project must be done in parent process (bash, fish, etc)"
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(let [res (->> (p/pipeline (p/pb  "list-projects")
                           (p/pb {:err :inherit} "fzf")) last deref)]
  (when (= (:exit res) 0)
    (->> res :out slurp str/trim println)))
