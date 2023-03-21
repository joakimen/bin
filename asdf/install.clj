#!/usr/bin/env bb
(ns asdf.install
  (:require [babashka.process :as p]
            [clojure.string :as str]))

;; install asdf-plugins, filtered through fzf

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (when (not (zero? exit))
      (throw (ex-info err {:babashka/exit exit})))
    (cond (string? out) (str/trim out) :else "")))

(defn list-languages []
  (->> (run "asdf" "plugin" "list")
       (str/split-lines)))

(defn list-versions [lang]
  (->> (run "asdf" "list" "all" lang)
       (str/split-lines)
       (assoc {} :lang lang :versions)))

(defn fzf [s]
  (let [{:keys [out exit]} @(p/process ["fzf" "-m"]
                                       {:in s :err :inherit
                                        :out :string})]
    (when (not (zero? exit))
      (System/exit exit))
    (cond (string? out) (str/trim out) :else "")))

(let [all-langs (list-languages)
      all-versions (mapv #(future (list-versions %)) all-langs)
      lang (->> all-langs (str/join "\n") fzf)
      version (->> all-versions (map deref) (filter #(= (:lang %) lang)) first :versions (str/join "\n") fzf)]
  (println "Installing" lang version)
  (p/shell "asdf" "install" lang version))
