(ns file.list-projects
  "find git-projects
   - configuration file: $XDG_CONFIG_HOME/projects.yaml"
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clj-yaml.core :as clj-yaml]
            [clojure.string :as str]))

(defn die [& args]
  (throw (ex-info (apply str args) {:babashka/exit 1})))

(let [bin "fd"]
  (when (not (fs/which bin))
    (die "missing required binary: " bin)))

(def config-file (str (fs/path (fs/xdg-config-home) "projects.yaml")))

(defn read-required-file
  "return contents of file. throw and exit if missing or empty."
  [file-path]
  (when (or (not (fs/exists? file-path)) (str/blank? file-path))
    (die "error: missing config file: " file-path))
  (let [contents (slurp file-path)]
    (when (str/blank? contents)
      (die "error: config file is empty: " file-path))
    contents))

(defn parse-config
  "parse contents of configuration file.

   expects the following structure:

   project:
     roots: (required)
       - ~/some-project
       - /home/moo/dev
     settings: (optional)
       - max-depth: int, recursion depth when travering roots for projects
    ignores:
       - node_modules
       - venv
   "
  [contents]
  (let [{project-root-entries :roots
         ignore-entries :ignores
         settings :settings} (:projects (clj-yaml/parse-string contents))]

    (when (empty? project-root-entries)
      (die "error: no project-entries defined in config-file: " config-file))

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

(let [config (-> config-file read-required-file parse-config)
      repos (-> config resolve-repos flatten doall)]

  (when (empty? repos)
    (die "couldn't resolve any repos from config: " config-file))

  (mapv println repos))

(comment

  (defn debug [s]
    (doto s (println)))
 ;; 
  )
