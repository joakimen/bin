(ns obsidian.review
  "do a health-check on a local obsidian-vault and report problems
   - files named Unnamed 
   - empty or less than 2 lines
   - no links/backlinks (can we do this?)
   
   1. Determine vaults
   2. For each vault, run checks
   3. Log problems with each file
   4. Parse log and take necessary action (delete files, print, ..)
   "

  (:require [babashka.fs :as fs]
            [clojure.edn :as edn]))

(def config-file (fs/file (fs/xdg-config-home) "notes.edn"))

(defn read-config []
  (-> config-file slurp edn/read-string))

(defn is-vault? [dir]
  (fs/directory? (fs/path dir ".obsidian")))

(defn list-subdirs [dir]
  (->> (fs/list-dir dir)
       (filter #(fs/directory? %))))

(defn is-empty-ish?
  "- if file has 0 lines, return true
   - if file has 1 line, and the line is a md h1-heading, return true
   else, return false"
  [path]
  (let [lines (fs/read-all-lines path)]
    (if (or (zero? (count lines))
            (and (= (count lines) 1)
                 (re-matches #"^#\s.*" (first lines))))
      true false)))

(defn is-untitled?
  "if file is named Untitled-something, return true
   else, return false"
  [path]
  (if (re-matches #"^Untitled(\s\d+)?\.md" (fs/file-name path))
    true false))

(defn check-vault
  "performs all actions (checks, cleanup, ..) for a given vault"
  [vault rule-fns]
  (->> (pmap #(% vault) rule-fns)
       (doall)))

(def operation-log (atom {}))

(defn log-operation! [problem file]
  (swap! operation-log update-in [file] conj problem))

(defn no-untitled
  "remove markdown-files whose name starts Untitled"
  [vault]
  (println "checking for untitled files in vault:" vault)
  (->> (fs/glob vault "**.md")
       (map str)
       (filter is-untitled?)
       (mapv #(log-operation! :untitled %))))

(defn no-empty
  "list files that are empty or have less than 2 lines"
  [vault]
  (println "checking for empty files in vault:" vault)
  (->> (fs/glob vault "**.md")
       (map str)
       (filter is-empty-ish?)
       (mapv #(log-operation! :empty %))))

(let [config (read-config)
      rule-fns (map #(% {:no_untitled no-untitled
                         :no_empty no-empty}) (:rules config))
      notes-path (fs/expand-home (:path config))
      vault-list (->> (list-subdirs notes-path)
                      (filter is-vault?))]
  (doall (pmap #(check-vault % rule-fns) vault-list))

  (->> @operation-log
       sort
       (mapv println)))

(comment

  (reset! operation-log {})

  ;;
  )
