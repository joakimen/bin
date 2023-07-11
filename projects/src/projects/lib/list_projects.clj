(ns projects.lib.list-projects
  "find git-projects
   - configuration file: $XDG_CONFIG_HOME/projects.edn"
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]))

(def config-file
  (str (fs/path (fs/xdg-config-home) "projects.edn")))

(defn- die [& args]
  (throw (ex-info (apply str args) {:babashka/exit 1})))

(defn- dir? [dir]
  (fs/directory? (fs/expand-home dir)))

(defn- slurp-edn [file]
  (-> file slurp edn/read-string))

(s/def ::roots (s/coll-of dir?))
(s/def ::ignores (s/coll-of string?))

(s/def ::max-depth pos?)
(s/def ::preview-cmd string?)

(s/def ::settings (s/keys :opt-un [::max-depth ::preview-cmd]))

(s/def ::config
  (s/keys
   :req-un [::roots]
   :opt-un [::settings ::ignores]))

(defn parse-config
  "parse contents of configuration file"
  [{:keys [projects]}]
  {:pre [(s/valid? ::config projects)]}
  (let [{:keys [roots ignores settings]} projects]
    (when (empty? roots)
      (die "error: no project-entries defined in config-file: " config-file))
    {:roots (->> (map (comp str fs/expand-home) roots)
                 (filter fs/directory?))
     :ignores (into [] ignores)
     :settings settings}))

(defn fd [{:keys [dir excludes] {:keys [max-depth]} :settings}]
  (let [exclude-string (map #(str "--exclude=" %) excludes)
        {:keys [out]} (apply p/sh {:dir dir}
                             "fd"
                             "--type" "directory"
                             "--glob" "**/.git"
                             "--absolute-path"
                             "--hidden"
                             "--max-depth" (or max-depth 3)
                             "--no-ignore" ;; don't respect global/user fd-ignorefiles 
                             exclude-string)]
    (->> out str/trim str/split-lines
         (filter #(not (str/blank? %)))
         (map (comp str fs/parent)))))

(defn resolve-repos
  "receives a cleaned-up version of project-config and return a vec of git repos from the defined roots"
  [{:keys [roots settings ignores]}]
  (->> (pmap #(fd {:dir %
                   :excludes ignores
                   :settings settings}) roots)
       (doall)))

(defn list-projects []
  (let [config (-> config-file slurp-edn parse-config)
        repos (-> config resolve-repos flatten)]
    (when (empty? repos)
      (die "couldn't resolve any repos from config: " config-file))
    repos))

(comment

  (list-projects)

  (defn debug [s]
    (doto s (println)))

  (edn/read-string (slurp (str (fs/path (fs/xdg-config-home) "projects.edn"))))
  ;;
  )  
