(ns obsidian.review
  {:clj-kondo/ignore true} ;;calva isn't updated yet, but clj-kondo now ignores this
  "do a health-check on a local obsidian-vault and report problems
   - files named Unnamed - empty or less than 2 lines"
  (:require [babashka.fs :as fs]
            [clj-yaml.core :as clj-yaml]))

(def config-file (str (fs/path (fs/xdg-config-home) "projects.yaml")))

(defn read-config []
  (->> config-file
       slurp
       clj-yaml/parse-string))


(defn parse-config [config]
  (let [notes-config (:notes config)
        existing-dirs (->> notes-config
                           :folders
                           (filter #(fs/directory? (fs/path (fs/expand-home %))))
                           (map str))]
    (println "existing dirs:"  existing-dirs)
    ;; ( (:folders notes-config)
    ;;      (map #(println "folder:" %)
    ;
    )
  ;
  )
  ;; notes
  ;;   root
  ;;   rules [list]
  ;; parse yaml, handle nulls, defaults, etc
  ;; - vaults [vector of paths]
  ;; - rules [no_untitled, no_empty]

(parse-config (read-config))

(defn get-vault [vault]
  ;; parse relpath to abspath
  ;; ensure dir exists and is not empty
  )

(defn list-vault-files [vault]
  ;; return vector of filenames in vault
  ;; - prereq: what is the structure of an obsidian vault? what to ignore?
  )

(defn isUntitledFile? [filepath]
  ;; return true if file is named "Untitled.*"
  ;; else false
  )

(defn isEmpty? [filepath]
  ;; return true if file is empty or has less than 1 line (the title)
  ;; else false
  )
