#!/usr/bin/env bb
(require '[babashka.process :refer [sh]]
         '[clojure.java.io :as io]
         '[clojure.string :as str])

;; WIP
;; requirements
;; - rebase-pull N repos by abspath
;; - async & gather results

;; pulls updates to the given list of repositories

(defn get-current-branch [repo]
  (-> (sh {:dir repo} "git branch --show-current")
      :out str/trim-newline))

(defn- pull-repo [repo]
  (let [branch (get-current-branch repo)
        cmd (format "git pull --rebase origin %s" branch)
        res (sh {:dir repo} cmd)]
    {:repo repo
     :out (:out res)
     :err (:err res)}))

(defn- get-repo-abs-path [repo-rel-path]
  (.getPath (io/file (System/getenv "HOME") "dev" repo-rel-path)))

(defn- zero-exit? [dir & args]
  (let [exit-code (:exit (apply sh {:dir dir} args))]
    (= exit-code 0)))

(defn- is-repo? [repo-path]
  (zero-exit? repo-path "git rev-parse @"))

(defn is-clean? [repo-path]
  (zero-exit? repo-path "git diff --quiet"))

(let [x "X"
      y "Y"
      z "Z"]
  (println (str x y z)))

(defn main []
  (let [repos ["github.com/joakimen/babashka-examples"
               "github.com/joakimen/shadow-cljs-example"
               "github.com/joakimen/jira-cli" ;; not a repo
               ]]
    (->> repos
         (map get-repo-abs-path)
         (filter is-repo?)
         (filter is-clean?)
         (map pull-repo)
         (map println))))

(main)

(comment

  (pull-repo  "/Users/joakim/dev/github.com/joakimen/babashka-examples")

  (let [repos ["github.com/joakimen/babashka-examples"
               "github.com/joakimen/shadow-cljs-example"
               "github.com/joakimen/jira-cli" ;; not a repo
               ]]
    (->> repos
         (map get-repo-abs-path)
         (filter is-repo?)
         (filter is-clean?)
         (map pull-repo)
         (map println)
        ;;  (map :out)
        ;;  
         ))
  ;; => ({:repo "/Users/joakim/dev/github.com/joakimen/babashka-examples",
  ;;      :out "On branch master\nnothing to commit, working tree clean\n",
  ;;      :err ""}
  ;;     {:repo "/Users/joakim/dev/github.com/joakimen/shadow-cljs-example",
  ;;      :out
  ;;      "On branch master\nChanges not staged for commit:\n  (use \"git add/rm <file>...\" to update what will be committed)\n  (use \"git restore <file>...\" to discard changes in working directory)\n\tmodified:   shadow-cljs.edn\n\tdeleted:    src/main/core.cljs\n\nUntracked files:\n  (use \"git add <file>...\" to include in what will be committed)\n\tout/\n\tsrc/main/app.cljs\n\tsrc/test/\n\nno changes added to commit (use \"git add\" and/or \"git commit -a\")\n",
  ;;      :err ""})
  )
