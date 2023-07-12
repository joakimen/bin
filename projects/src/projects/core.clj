(ns projects.core
  (:require [babashka.fs :as fs]
            [clojure.string :as str]
            [fzf.core :refer [fzf]]
            [projects.config :as config]
            [projects.lib.build :as build]
            [projects.lib.guess :refer [guess-project-type]]
            [projects.lib.list-projects :as l :refer [fd]]))

(defn project-shortname [project]
  (->> project
       (fs/components)
       (take-last 2)
       (str/join "/")))

(defn guess-type []
  (let [cur-dir (str (fs/cwd))]
    (guess-project-type cur-dir)))

(defn list-projects []
  (l/list-projects))

(defn build-projects []
  (let [projects (l/list-projects)]
    (->> projects
         (map #(assoc {} :proj-path % :proj-type (guess-project-type %)))
         (filter #(not= (:proj-type %) :unknown))
         (pmap build/build-project)
         (doall)
         (remove nil?))))

(defn delete-projects []
  (let [{:keys [clone-dir excludes settings]} (config/read-config)

        projects (->> (fd {:dir clone-dir
                           :excludes excludes
                           :settings settings}))
        projects-to-delete (fzf {:multi true} projects)]
    (when-not (empty? projects-to-delete)
      (println "Deleting" (count projects-to-delete) "project(s)")
      (doseq [proj projects-to-delete]
        (println "-" (project-shortname proj))
        (fs/delete-tree (str proj))))))
