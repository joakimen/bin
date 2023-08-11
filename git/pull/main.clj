#!/usr/bin/env bb
(ns git.pull.main
  "WIP - rebase-pull all tracked repos. dirty repos are skipped"
  (:require [babashka.fs :as fs]
            [babashka.process :refer [sh]]
            [clojure.pprint :as pprint]
            [clojure.string :as str]
            [doric.core :as doric]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply sh args)]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (str/trim out)))

(defn list-projects []
  (str/split-lines (run "list-projects")))

(defn zero-exit? [dir & args]
  (zero? (:exit (apply sh {:dir dir} args))))

(defn- is-clean? [repo-path]
  (zero-exit? repo-path "git diff --quiet"))

(defn- get-current-branch [repo]
  (run "git" "-C" repo "branch" "--show-current"))

(defn- git-pull [repo branch]
  (sh "git" "-C" repo "pull" "--rebase" "--autostash" "origin" branch))

(defn- pull-repo [repo]
  (let [branch (get-current-branch repo)
        {:keys [exit out err]} (git-pull repo branch)]
    {:repo repo
     :branch branch
     :exit exit
     :err err
     :out out}))

(defn- parse-repo-shortname [s]
  (->> (fs/components s)
       (take-last 2)
       (map str)
       (str/join "/")))

(defn- trunc
  [s n]
  (subs s 0 (min (count s) n)))

(defn- fmt-msg [msg]
  (-> msg str/trim str/split-lines first (trunc 50)))

(defn prettify [project]
  (let [{:keys [err exit repo]} project]
    {:repo (parse-repo-shortname repo)
     :exit exit
     :err (fmt-msg err)}))

(defn -main [& _]
  (let [clean-projects (filter is-clean? (list-projects))
        executor (Executors/newFixedThreadPool 128)
        tasks (mapv #(fn [] (pull-repo %)) clean-projects)
        execution-results (->> (.invokeAll ^ExecutorService executor tasks)
                               (map #(.get ^Future %)))
        result (mapv prettify execution-results)
        cols (cond-> [:repo :exit]
               (some #(not= (:exit %) 0) result) (conj :err))
        table (doric/table cols result)]
    (println table)))

(-main)

(comment

  (def clean-projects (filter is-clean? (list-projects)))
  (def pulled (->> clean-projects
                   (take 5)))
  pulled
  (def executor (Executors/newVirtualThreadPerTaskExecutor))
  (def tasks (mapv #(fn [] (pull-repo %)) clean-projects))
  (->> (.invokeAll ^ExecutorService executor tasks)
       (map #(.get ^Future %)))

  tasks
  (def threads (.invokeAll ^ExecutorService executor tasks))
  (def exec-results (map #(.get ^Future %) threads))

  (->> (.invokeAll ^ExecutorService executor tasks)
       (map #(.get ^Future %)))

 ;; 
  )
