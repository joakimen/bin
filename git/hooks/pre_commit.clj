#!/usr/bin/env bb
(ns git.hooks.pre-commit
  (:require [babashka.process :as p]
            [clojure.string :as str]
            [babashka.fs :as fs]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (when-not (zero? exit)
      (throw (ex-info err {:babashka/exit exit})))
    (str/trim out)))

(defn list-staged-files []
  (->> (run "git diff --name-only --cached --diff-filter=d") str/trim str/split-lines))

(defmulti action
  "commands to run on pre-commit events for a given language"
  (fn [lang] lang))

(defn in? [coll e]
  (some #(= e %) coll))

(defmulti lint (fn [file] (:type file)))

(defmethod lint "clojure" [{:keys [path]}]
  (let [{:keys [out err exit]} (p/sh "clj-kondo" "--lint" path)]
    (cond
      (zero? exit) (str/trim out)
      (in? [2 3] exit) (throw (ex-info (str/trim out) {:babashka/exit exit}))
      :else (throw (ex-info (str/trim err) {:babashka/exit exit})))))

(defmethod action "maven" [_]
  (p/shell "mvn" "spotless:check"))

(defmethod action "go" [_]
  (p/shell "go" "test" "-v"))

(defmethod action "clojure" [_]

  ;; lint staged clojure-files
  (->> (list-staged-files)
       (filter #(in? ["clj" "cljc" "cljs"] (fs/extension %)))
       (map #(assoc {} :type "clojure" :path %))
       (mapv lint))

  ;; (p/shell "fd" "-e" "clj" "-x" "cljfmt" "fix")
  ;; (p/shell "clj-kondo" "--lint" ".")
  )

;; default - noop
(defmethod action :default [_])

(action (run "guess-project"))

(comment
  (run "guess-project") ;; => clojure
  ;;
  )
