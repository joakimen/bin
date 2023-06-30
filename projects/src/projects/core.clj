(ns projects.core
  (:require [babashka.fs :as fs]
            [projects.lib.build :as build]
            [projects.lib.guess :refer [guess-project-type]]
            [projects.lib.list-projects :as l]))

(defn guess-type []
  (let [cur-dir (str (fs/cwd))]
    (prn (guess-project-type cur-dir))))

(defn list-projects []
  (prn (l/list-projects)))

(defn build-projects []
  (let [projects (l/list-projects)]
    (->> projects
         (map #(assoc {} :proj-path % :proj-type (guess-project-type %)))
         (filter #(not= (:proj-type %) :unknown))
         (pmap build/build-project)
         (doall)
         (remove nil?))))
