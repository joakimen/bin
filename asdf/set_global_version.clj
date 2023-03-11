#!/usr/bin/env bb
(ns asdf.set-global-version
  (:require [babashka.process :as p]
            [clojure.string :as str]))

;; set global version of an asdf-plugin, filtered through fzf

(defn run [& args]
  (let [res (apply p/sh args)]
    (when (> (:exit res) 0)
      (->> res :err str/trim println)
      (System/exit (:exit res)))
    (->> res :out str/trim)))

(defn list-languages []
  (->> (run "asdf" "plugin" "list")))

(defn list-versions [lang]
  (->> (run "asdf" "list" lang)))

(defn fzf [s]
  (let [res @(p/process ["fzf" "-m"]
                        {:in s :err :inherit
                         :out :string})]
    (when (> (:exit res) 0)
      ;; this is usually because of a Ctrl-C and doesnt
      ;; warrant printing stderrr
      (System/exit (:exit res)))
    (->> res :out str/trim)))

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
