#!/usr/bin/env bb
(ns git.pull
  "
   WIP

   pulls updates to the given list of repositories
 
   # requirements
 
   determine projects (hard-coded list is not feasible)
   - dev-root from stdin
     - alt 1) dev-root argument (e.g. ~/dev) + use hard-coded recursion depth
     - alt 2) dev-root env var (e.g. ~/dev) + use hard-coded recursion depth
   - configuration file
     - alt 1) dir (+ depth?)
       - 1 level sounds fair
     - which format? json/yaml/toml/text
       - probably yaml for this
 
   choosing
   - yaml configuration file
   - entries = dirnames
   - 1 level depth, e.g.
     - dirname: /dev/github
     - resolves
       - /dev/github/proj-a
       - /dev/github/proj-b
 
   pull-operation
   - rebase-pull N repos by abspath
   - async & gather results (pmap / async channels?)
   "
  (:require [babashka.process :refer [sh]]
            [clj-yaml.core :as clj-yaml]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [babashka.fs :as fs]
            [taoensso.timbre :as log]))

(def config-file (str (System/getenv "HOME") "/.config/pullconf.yaml"))

(defn read-config []
  (slurp config-file))

(defn parse-yaml [s]
  (clj-yaml/parse-string s))

(defn zero-exit? [dir & args]
  (let [exit-code (:exit (apply sh {:dir dir} args))]
    (= exit-code 0)))

(defn repo? [repo-path]
  (zero-exit? repo-path "git rev-parse @"))

(defn clean? [repo-path]
  (zero-exit? repo-path "git diff --quiet"))

(defn get-current-branch [repo]
  (-> (sh {:dir repo} "git branch --show-current")
      :out str/trim-newline))

(defn- pull-repo [repo]
  (println "pulling repo:" repo)
  ;; (let [branch (get-current-branch repo)
  ;;       cmd (format "git pull --rebase --autostash origin %s" branch)
  ;;       res (sh {:dir repo} cmd)]
  ;;   {:repo repo
  ;;    :exit (:exit res)
  ;;    :out (:out res)
  ;;    :err (:err res)})
 ;; 
  )

(defn git-status [repo]
  (let [cmd (format "git -C %s status --short" repo)
        res (sh {:dir repo} cmd)]
    {:repo repo
     :exit (:exit res)
     :out (:out res)
     :err (:err res)}))

;; 1. read config file
(let [dir-entries (->> (read-config)
                       (parse-yaml)
                       :dirs
                       (map fs/expand-home))]

 ;; warn for non-existent dir entries in 
  (->> dir-entries
       (filter #(not (fs/directory? %)))
       (mapv #(log/warn (str "no such dir: " % ", skipping"))))


  ;; git-pull the existing ones
  (->> dir-entries
       (filter fs/directory?)
       (pmap fs/list-dir)
       (flatten)
       (filter #(not (fs/hidden? %)))
       (map str)
       (filter repo?)
       (filter clean?)
       (pmap git-status)
       (mapv println)

;;
       )
 ;; 
  )
