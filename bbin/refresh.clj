#!/usr/bin/env bb
(ns bbin.refresh
  (:require [babashka.fs :as fs]
            [babashka.process :as p]))

;; desired packages, until bbin supports a declarative package config

(defn in? [coll e]
  (some #(= e %) coll))

(let [ignores ["obsidian/review.clj"]
      remotes ["https://raw.githubusercontent.com/borkdude/tools/main/cljfmt.clj"]
      locals (->> (fs/glob "." "**.clj")
                  (map str)
                  (filter #(not (in? ignores %))))
      to_install (distinct (into remotes locals))]
  (mapv #(p/shell "bbin" "install" %) to_install))
