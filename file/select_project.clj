(ns file.select-project
  "select a project, filtered through fzf
   cd'ing to project must be done in parent process (bash, fish, etc)"
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.string :as str]
            [clj-yaml.core :as clj-yaml]))

(def config-file (str (fs/path (fs/xdg-config-home) "projects.yaml")))

(let [settings (->> config-file slurp clj-yaml/parse-string :settings)
      {:keys [exit out]} (->> (p/pipeline (p/pb  "list-projects")
                                          (p/pb {:err :inherit} "fzf"
                                                "--preview"
                                                (or (:preview-cmd settings) "ls -1 {}")
                                                "--height" "50%"))
                              last deref)]

  (when-not (zero? exit)
    (->> out slurp str/trim println)))
