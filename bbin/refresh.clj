#!/usr/bin/env bb
(ns bbin.refresh
  "desired packages, until bbin supports a declarative package config"
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [edamame.core :as eda]))

(defn in? [coll e]
  (some #(= e %) coll))

;; install scripts
(let [ignores ["obsidian/review.clj"
               "bbin/refresh.clj"]
      remotes ["https://raw.githubusercontent.com/borkdude/tools/main/cljfmt.clj"]
      locals (->> (fs/glob (fs/path (fs/home) "bin") "**.clj")
                  (map str)
                  (filter #(not (in? ignores %))))
      to_install (distinct (into remotes locals))]
  (doall (pmap #(p/shell "bbin" "install" %) to_install)))

;; list installations
(let [res (:out (p/sh {:out :string} "bbin" "ls"))
      parsed (eda/parse-string res)]
  (->> parsed
       (map #(assoc {} :script (key %) :coords (-> (val %) :coords :bbin/url)))
       (sort-by :script)
       (mapv #(println (:script %) (:coords %)))))
