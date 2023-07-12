(ns projects.lib.list-projects
  "find git-projects"
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [projects.config :as config]))

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
  (let [config (config/read-config)
        repos (-> config resolve-repos flatten)]
    (when (empty? repos)
      (throw (ex-info "couldn't resolve any repos from config" {:babashka/exit 1})))
    repos))

(comment

  (list-projects)

  (defn debug [s]
    (doto s (println)))

  (edn/read-string (slurp (str (fs/path (fs/xdg-config-home) "projects.edn"))))
  ;;
  )
