(ns projects.lib.build
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.string :as str]))

(defmulti get-build-cmd (fn [proj-type] proj-type))

(defmethod get-build-cmd :maven [_]
  "mvn clean verify")

(defmethod get-build-cmd :default [_]
  nil)

(defn- get-shortname [p]
  (->> (fs/components p)
       (take-last 2)
       (str/join "/")))

(defn build-project [{:keys [proj-type proj-path]}]
  (when-let [build-cmd (get-build-cmd proj-type)]
    (println (format "building: %s ($ %s)" (get-shortname proj-path) build-cmd))
    (let [{:keys [exit]} (p/sh {:dir proj-path} build-cmd)]
      {:proj-path proj-path
       :proj-type proj-type
       :build-cmd build-cmd
       :exit exit})
   ;; 
    ))
