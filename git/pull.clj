#!/usr/bin/env bb
(ns git.pull
  "WIP - ebase-pull all tracked repos. dirty repos are skipped"
  (:require [babashka.process :refer [sh]]
            [clojure.pprint :as pprint]
            [clojure.string :as str]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply sh args)]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (str/trim out)))

(defn list-projects []
  (str/split-lines (run "list-projects")))

(defn zero-exit? [dir & args]
  (zero? (:exit (apply sh {:dir dir} args))))

(defn is-clean? [repo-path]
  (zero-exit? repo-path "git diff --quiet"))

(defn get-current-branch [repo]
  (run "git" "-C" repo "branch" "--show-current"))

(defn- pull-repo [repo]
  (let [branch (get-current-branch repo)]
    (println "pulling repo:" repo "branch:" branch)
    (assoc (select-keys (sh "git" "-C" repo "pull" "--rebase" "--autostash" "origin" branch) [:out :err :exit]) :repo repo)))

(defn parse-repo-shortname [s]
  (let [pat #".*/([a-zA-Z0-9]+/[a-zA-Z0-9-]+)$"
        res (second (re-find pat s))]
    (when (nil? res)
      (throw (ex-info (str "failed to parse repo shortname from: " s) {:babashka/exit 1})))
    res))

(->> (list-projects)
     (filter is-clean?)
     (pmap pull-repo)
     (map #(update % :repo parse-repo-shortname)) ;; shorten name for printing
     (map #(dissoc % :out :err)) ;; until we have a better way to print this
     (doall)
     (pprint/print-table))

(comment

  (def p (list-projects))

  (->> p
       (filter is-clean?)
       (pmap pull-repo)
       (map #(update % :repo parse-repo-shortname))
       (map #(dissoc % :out :err))
       (doall)
       (pprint/print-table))

 ;; 
  )
