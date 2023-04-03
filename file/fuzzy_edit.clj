#!/usr/bin/env bb
(ns file.fuzzy-edit
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.string :as str]))

(let [bin "fzf"]
  (when-not (fs/which bin)
    (throw (ex-info (str "missing required binary: " bin) {:babashka/exit 1}))))

(def preview-commands
  [{:pri 1 :bin "bat" :cmd "bat --line-range :20 --number --color=always {}"}
   {:pri 2 :bin "cat" :cmd "cat {}"}])

(defn preview-cmd []
  (->> preview-commands
       (filter #(fs/which (:bin %)))
       (sort-by :pri) first :cmd))

(defn fzf [in]
  (let [{:keys [exit out]}
        @(p/process
          ["fzf" "-m" "--preview"
           in "--preview-window" "50%"]
          {:in :inherit
           :out :string
           :err :inherit})]
    (when-not (zero? exit)
      (System/exit 0))
    (-> out str/trim str/split-lines)))

(apply p/shell "nvim" (fzf (preview-cmd)))

(comment
  (defn debug [s]
    (doto s (println)))
  ;
  )
