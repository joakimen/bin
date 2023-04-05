#!/usr/bin/env bb
(ns bbin.refresh
  "desired packages, until bbin supports a declarative package config"
  (:require [babashka.fs :as fs]
            [babashka.process :as p]))

(defn in? [coll e]
  (some #(= e %) coll))

(let [ignores ["obsidian/review.clj"
               "bbin/refresh.clj"]
      remotes ["https://raw.githubusercontent.com/borkdude/tools/main/cljfmt.clj"]
      locals (->> (fs/glob (fs/path (fs/home) "bin") "**.clj")
                  (map str)
                  (filter #(not (in? ignores %))))
      to_install (distinct (into remotes locals))]
  (mapv #(p/shell "bbin" "install" %) to_install))
