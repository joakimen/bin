(ns obsidian.doctor.main
  "WIP
   
   do a health-check on a local obsidian-vault and report problems
   - files named Unnamed 
   - empty or just a markdown heading
   - no links/backlinks (can we do this?)
   
   1. Determine vaults
   2. For each vault, run checks
   3. Log problems with each file
   4. Parse log and take necessary action (delete files, warn user, ..)
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

(def operation-log (atom {}))

(defn log-vault [vault]
  (swap! operation-log assoc vault {}))

(defn log-file [vault file]
  (swap! operation-log update-in [vault] conj {file []}))

(defn log-file-problem [vault file problem]
  (swap! operation-log update-in [vault file] conj problem))

(defn check-vault
  "performs all actions (checks, cleanup, ..) for a given vault"
  [vault rule-fns]
  (log-vault vault)

  (let [files (map str (fs/glob vault "**.md"))]
    (mapv #(log-file vault %) files)
    (->> (pmap #(% vault files) rule-fns)
         (doall))))


(defn no-untitled
  "remove markdown-files whose name starts Untitled"
  [vault files]
  (println "checking for untitled files in vault:" (str vault))
  (->> files
       (filter is-untitled?)
       (mapv #(log-file-problem  vault % :untitled))))

(defn no-empty
  "list files that are empty or have less than 2 lines"
  [vault files]
  (println "checking for empty files in vault:" (str vault))
  (->> files
       (filter is-empty-ish?)
       (mapv #(log-file-problem vault % :empty))))

(defn check-vaults []
  (let [config (read-config)
        rule-fns (map #(% {:no_untitled no-untitled
                           :no_empty no-empty}) (:rules config))
        notes-path (fs/expand-home (:path config))
        vault-list (->> (list-subdirs notes-path)
                        (filter is-vault?)
                        (map str))]
    (doall (pmap #(check-vault % rule-fns) vault-list))))

(defn scrub-log []) ;; remove files from log that have no problems

(check-vaults)
(scrub-log)
;; (clean-vaults)

(comment

  (reset! operation-log {})

 ;; 
  )
