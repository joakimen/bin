#!/usr/bin/env bb
(ns asdf.set-global-version
  (:require [babashka.process :as p]
            [clojure.string :as str]))

;; set global version of an asdf-plugin, filtered through fzf

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (when (not (zero? exit))
      (throw (ex-info err {:babashka/exit exit})))
    (cond (string? out) (str/trim out) :else "")))

(defn list-languages []
  (->> (run "asdf" "plugin" "list")))

(defn list-versions [lang]
  (->> (run "asdf" "list" lang)))

(defn fzf [s]
  (let [{:keys [out exit]}
        @(p/process ["fzf" "-m"]
                    {:in s :err :inherit
                     :out :string})]
    (when (not (zero? exit))
      ;; this is usually because of a Ctrl-C and doesnt
      ;; warrant printing stderrr
      (System/exit exit))
    (cond (string? out) (str/trim out) :else "")))

(let [lang (fzf (list-languages))
      version (->> (list-versions lang)
                   str/split-lines
                   (map #(str/replace % #"[\*\s]*" "")) ;; remove leading whitespace and asterisks
                   (str/join "\n")
                   fzf)]
  (println "setting" lang "version" version "as global default")
  (p/shell "asdf" "global" lang version))

(comment
  (str/replace "   *5.23.10" #"[\*\s]*" "")
 ;; 
  )
