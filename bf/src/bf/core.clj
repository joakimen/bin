(ns bf.core
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.string :as str]
            [fzf.core :refer [fzf]]))

(defn fuzzy-edit []
  (let [preview-commands [{:pri 1 :bin "bat" :cmd "bat --line-range :20 --number --color=always {}"}
                          {:pri 2 :bin "cat" :cmd "cat {}"}]
        preview-cmd (->> preview-commands
                         (filter #(fs/which (:bin %)))
                         (sort-by :pri) first :cmd)
        files (fzf {:multi true
                    :preview preview-cmd})]
    (when (empty? files)
      (throw (ex-info "no files selected" {:babashka/exit 1})))
    (println "opening files:")
    (run! #(println "-" %) files)
    (apply p/shell "nvim" "-O" files)))

(defn get-file-path []
  (when-let [file (fzf)]
    (-> file fs/absolutize str println)))

(defn get-file-parent-path []
  (when-let [file (fzf)]
    (-> file fs/absolutize fs/parent str println)))

(defn fuzzy-cat []
  (when-let [file (fzf)]
    (-> file slurp println)))

(defn fuzzy-rm []
  (let [files (fzf {:multi true})]
    (when (empty? files)
      (throw (ex-info "no files selected" {:babashka/exit 1})))
    (println "deleting files")
    (doseq [f files]
      (println "-" f)
      (fs/delete-if-exists f))))

(defn fuzzy-git []
  (let [file (fzf)]
    (when file
      (p/shell "lazygit" "-f" file))))

(defn select-dir []
  (let [dirs (-> (p/sh "fd" "-t" "d")
                 :out str/split-lines)]
    (when (empty? dirs)
      (throw (ex-info "no dirs found" {:babashka/exit 1})))
    (-> (fzf {:in dirs})
        fs/absolutize str println)))
