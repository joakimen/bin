#!/usr/bin/env bb
(require '[babashka.fs :as fs]
         '[babashka.process :as p]
         '[clojure.string :as str])

(defn start-process [cmd-vec in]
  (let [proc (p/process cmd-vec
                        {:in in :err :inherit
                         :out :string})]
    (:out @proc)))

(defn fzf [input]
  (start-process ["fzf" "-m" "--preview"
                  "bat --line-range :20 --number --color=always {}"] input))
(let [files (->> (fs/glob "." "**")
                 (filter fs/regular-file?)
                 (str/join "\n"))
      file (fzf files)]
  (p/shell (format "nvim %s" file)))
