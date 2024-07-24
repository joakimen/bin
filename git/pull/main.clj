 (ns git.pull.main
  "rebase-pull main branches of all tracked repos.

   Repos are pulled if
   - repo has a supported remote
   - work tree is clean
   - checked out branch is master/main

   idea: split into 3 parts:
   - collect all repo metadata (branch, remote, clean, etc)
   - validate repos by inspecting metadata
    - pull repos in parallel"
  (:require [babashka.fs :as fs]
            [babashka.process :refer [sh]]
            [clojure.string :as str]
            [doric.core :as doric])
  (:import [java.util.concurrent Executors ExecutorService Future]))

(def ^:const supported-remotes
  ["git@github.com"
   "https://github.com"])

(defn- run [& args]
  (let [{:keys [out err exit]} (apply sh args)]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (str/trim out)))

(defn list-projects []
  (when-let [projects (-> (run "list-projects")
                          (str/split-lines)
                          (seq))]
    projects))

(defn zero-exit? [dir & args]
  (zero? (:exit (apply sh {:dir dir} args))))

(defn- is-clean? [repo-path]
  (zero-exit? repo-path "git diff --quiet"))

(defn- get-repo-remote [repo]
  (:out (sh "git" "-C" repo "config" "--get" "remote.origin.url")))

(defn- get-current-branch [repo]
  (run "git" "-C" repo "branch" "--show-current"))

(defn- git-pull [repo branch]
  (sh "git" "-C" repo "pull" "--rebase" "--autostash" "origin" branch))

(defn- trunc
  [s n]
  (subs s 0 (min (count s) n)))

(defn has-supported-remote? [repo]
  (let [substr-in? (fn [e coll]
                     (some #(str/includes? e %) coll))
        supported-remote? #(substr-in? % supported-remotes)
        remote-url (get-repo-remote repo)]
    (when remote-url
      (supported-remote? remote-url))))

(defn is-on-default-branch? [repo]
  (let [branch (get-current-branch repo)]
    (some #{branch} ["master" "main"])))

(defn- pull-repo [repo]
  (let [branch (get-current-branch repo)
        {:keys [exit out err]} (git-pull repo branch)]
    {:repo repo
     :branch (trunc branch 20)
     :exit exit
     :err err
     :out out}))

(defn- parse-repo-shortname [s]
  (->> (fs/components s)
       (take-last 2)
       (map str)
       (str/join "/")))

(defn- fmt-msg [msg]
  (-> msg str/trim str/split-lines first (trunc 50)))

(defn prettify [{:keys [branch err exit repo]}]
  {:repo (parse-repo-shortname repo)
   :branch branch
   :exit exit
   :err (fmt-msg err)})

(defn -main [& _]
  (let [projects (list-projects)
        filtered-projects
        (filter (apply every-pred
                       [is-clean?
                        has-supported-remote?
                        is-on-default-branch?])
                projects)
        executor (Executors/newFixedThreadPool 128)
        tasks (mapv #(fn [] (pull-repo %)) filtered-projects)
        execution-results (->> (.invokeAll ^ExecutorService executor tasks)
                               (map #(.get ^Future %)))
        result (mapv prettify execution-results)
        cols (cond-> [:repo :branch :exit]
               (some #(not= (:exit %) 0) result) (conj :err))
        table (doric/table cols result)]
    (println table)))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))

(comment

  (def projects (list-projects))
  (def clean-projects (filter is-clean? projects))
  (def supported-remote-projects (filter has-supported-remote? projects))

  (filter (apply every-pred [is-clean? has-supported-remote?]) projects)

  (->> clean-projects
       (filter #(not (has-supported-remote? %))))

  (get-repo-remote (nth clean-projects 60))
  (has-supported-remote? (nth clean-projects 60))
  (-> clean-projects last has-supported-remote?)

  (-> clean-projects last get-repo-remote)
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

  :rcf)
