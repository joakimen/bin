(ns bbfile.core
  (:require [fzf.core :refer [fzf]]
            [babashka.fs :as fs]
            [babashka.process :as p]))

(defn fuzzy-edit []
  (let [preview-commands [{:pri 1 :bin "bat" :cmd "bat --line-range :20 --number --color=always {}"}
                          {:pri 2 :bin "cat" :cmd "cat {}"}]
        preview-cmd (->> preview-commands
                         (filter #(fs/which (:bin %)))
                         (sort-by :pri) first :cmd)
        files (fzf {:multi true
                    :preview preview-cmd})]
    (when (empty? files)
      (throw (ex-info "no files selected" {})))
    (println "opening files:")
    (mapv #(println "-" %) files)
    (apply p/shell "nvim" "-O" files)))

(defn get-file-dir []
  (-> (fzf {:mode :file-single})
      fs/absolutize fs/parent str println))
