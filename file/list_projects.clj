(ns file.list-projects
  "find git-projects
   - configuration file: $XDG_CONFIG_HOME/projects.yaml"
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clj-yaml.core :as clj-yaml]
            [clojure.string :as str]))

(let [bin "fd"]
  (when (not (fs/which bin))
    (println "missing required binary:" bin)
    (System/exit 1)))

(def config-file (str (fs/path (fs/xdg-config-home) "projects.yaml")))

(defn err-exit [& args]
  (apply println args)
  (System/exit 1))

(defn read-required-file
  "return contents of file. throw and exit if missing or empty."
  [file-path]
  (when (or (not (fs/exists? file-path)) (str/blank? file-path))
    (err-exit "error: no such file:" file-path))
  (let [contents (slurp file-path)]
    (when (str/blank? contents)
      (err-exit "error: file is empty:" file-path))
    contents))

(defn parse-config
  "parse contents of configuration file

   project-roots:
    - ensure at least one project-root is defined
    - filter out invalid values (non-existing dirs etc)
 
   ignores:
   - return empty vec by default, populate with optional entries from config-file

   settings:
     max-depth: recursion-depth when searching for repositories
   "
  [contents]
  (let [{project-root-entries :project-roots
         ignore-entries :ignores
         settings :settings} (clj-yaml/parse-string contents)]

    (when (empty? project-root-entries)
      (err-exit "error: no project-entries defined in config-file:" config-file))

    {:project-roots (->> project-root-entries
                         (map (comp str fs/expand-home))
                         (filterv fs/directory?))
     :ignores (into [] ignore-entries)
     :settings settings}))

(defn fd [{:keys [dir excludes settings]}]
  (let [exclude-string (->> excludes (map #(str "--exclude=" %)))
        res (apply p/sh {:dir dir}
                   "fd"
                   "--type" "directory"
                   "--glob" "**/.git"
                   "--absolute-path"
                   "--hidden"
                   "--max-depth" (or (:max-depth settings) 3)
                   "--no-ignore" ;; don't respect global/user fd-ignorefiles 
                   exclude-string)]
    (->> res :out str/trim str/split-lines
         (filter #(not (str/blank? %)))
         (mapv (comp str fs/parent)))))

(defn resolve-repos
  "read a cleaned-up version of project-config and return a vec of git repos from the defined roots"
  [opts]
  (->> (:project-roots opts)
       (pmap #(fd {:dir %
                   :excludes (:ignores opts)
                   :settings (:settings opts)}))))

(->> (read-required-file config-file)
     (parse-config)
     (resolve-repos)
     (flatten)
     (mapv println))

(comment

  (defn debug [s]
    (doto s (println)))
 ;; 
  )
