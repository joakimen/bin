(ns obsidian.review
  {:clj-kondo/ignore true} ;;calva isn't updated yet, but clj-kondo now ignores this
  "do a health-check on a local obsidian-vault and report problems
   - files named Unnamed - empty or less than 2 lines")

(def config-file "TODO")

(defn read-config []
  ;; TODO: read config-file to string
  )

(defn obsidian-review/parse-yaml [contents]
  ;; TODO parse string to yaml
  )

(defn parse-config [config-yaml]
  ;; parse yaml, handle nulls, defaults, etc
  ;; - vaults [vector of paths]
  ;; - rules [no_untitled, no_empty]
  )

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
