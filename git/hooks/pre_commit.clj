#!/usr/bin/env bb
(ns git.hooks.pre-commit
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (str/trim out)))

(defmulti action
  "commands to run on pre-commit events for a given language"
  (fn [lang] lang))

(defmethod action "maven" [_]
  (p/shell "mvn" "spotless:check"))

(defmethod action "go" [_]
  (p/shell "go" "test" "-v"))

(defmethod action "clojure" [_]
  (p/shell "fd" "-e" "clj" "-x" "cljfmt" "fix")
  (p/shell "clj-kondo" "--lint" "."))

;; default - noop
(defmethod action :default [_])

(action (run "guess-project"))

(comment
  (run "guess-project") ;; => clojure
  ;;
  )
