(ns list-project-branches.main
  (:require [babashka.process :as p]
            [clojure.string :as str]
            [doric.core :as doric]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (if (zero? exit)
      (str/trim out)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))))

(defn get-current-branch [repo]
  (run "git" "-C" repo "branch" "--show-current"))

(let [project-list (-> (run "list-projects") (str/split-lines))
      branches (doall (pmap #(assoc {} :project % :branch (get-current-branch %)) project-list))]
  (println (doric/table branches)))
