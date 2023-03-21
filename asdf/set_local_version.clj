#!/usr/bin/env bb
(ns asdf.set-local-version
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn fzf [s]
  (let [{:keys [out exit]}
        @(p/process ["fzf"]
                    {:in s :err :inherit
                     :out :string})]
    (when (not (zero? exit))
      (System/exit exit))
    (cond (string? out) (str/trim out) :else "")))

(defn fzfv [v]
  (->> (str/join "\n" v)
       (fzf)
       (str/split-lines)))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (when (not (zero? exit))
      (throw (ex-info err {:babashka/exit exit})))
    (cond (string? out) (str/trim out) :else "")))

(defn list-languages []
  (->> (run "asdf plugin list")
       (str/split-lines)))

(defn list-versions [lang]
  (let [versions (->>
                  (run "asdf" "list" lang)
                  (str/split-lines)
                  (mapv #(str/replace % #"[\s\*]" "")))]
    (assoc {} :lang lang :versions versions)))

(let [all-langs (list-languages)
      installed-versions (mapv #(future (list-versions %)) all-langs)
      lang (->> all-langs fzfv first)
      version (->> installed-versions
                   (map deref)
                   (filter #(= (:lang %) lang))
                   first :versions (str/join "\n") fzf)]
  (p/shell "asdf" "local" lang version))
