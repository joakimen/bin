#!/usr/bin/env bb
(ns git.hooks.pre-commit
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (str/trim out)))

;; define multimethod to run hook based on project type
(defmulti action (fn [project] [(:type project)]))

;; maven
(defmethod action ["maven"] [_]
  (p/shell "mvn" "spotless:check"))

;; go
(defmethod action ["go"] [_]
  (p/shell "go" "test" "-v"))

(defmethod action ["clojure"] [_]
  ;;(p/shell "cljfmt" "fix" ".")
  (p/shell "clj-kondo" "--lint" "."))

;; default
(defmethod action :default [project]
  (throw (IllegalArgumentException.
          (str "unsupported project type: " project))))

(action {:type (run "guess-project")})

(comment
  (action {:type "maven"})

  (let [project-type (run "guess-project")]
    (println "project:" project-type))
  ;; project: clojure
 ;; 
  )
