(ns bbin.bbiz
  "(hopefully) short-lived convenience script to install clj-scripts using bbin"
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.string :as str]))


(defn install [file]
  (p/shell "bbin" "install" file))

(let [path (str (fs/path (fs/home) "bin"))
      pattern "**{.clj,.bb}"
      files (->> (fs/glob path pattern) (str/join "\n"))]
  (->> (p/shell {:err :inherit
                 :in files
                 :out :string} "fzf" "-m")
       :out str/trim str/split-lines
       (pmap install) doall))
