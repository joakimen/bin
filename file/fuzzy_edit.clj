#!/usr/bin/env bb
(ns file.fuzzy-edit
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.string :as str]))

(let [bin "fzf"]
  (when (not (fs/which bin))
    (println "missing required binary:" bin)
    (System/exit 1)))

(def preview-commands
  [{:pri 1 :bin "bat" :cmd "bat --line-range :20 --number --color=always {}"}
   {:pri 2 :bin "cat" :cmd "cat {}"}])

(defn fzf []
  (let [preview-cmd (->> preview-commands
                         (filter #(fs/which (:bin %)))
                         (sort-by :pri) first :cmd)
        proc @(p/process ["fzf" "-m" "--preview"
                          preview-cmd "--preview-window" "50%"]
                         {:in :inherit
                          :out :string
                          :err :inherit})]
    (when (> (:exit proc) 0)
      (System/exit 0))
    (-> proc :out str/trim str/split-lines)))

(apply p/shell "nvim" (fzf))

(comment
  (defn debug [s]
    (doto s (println)))
  ;
  )
