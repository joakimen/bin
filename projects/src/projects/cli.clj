(ns projects.cli
  (:require [projects.core :as core]))

(defn build
  "Build all tracked projects"
  [_]
  (core/build-projects))

(defn guess-type
  "Guess which type of project this is"
  [_]
  (core/guess-type))

(defn list-projects
  "List all tracked projects"
  [_]
  (core/list-projects))
