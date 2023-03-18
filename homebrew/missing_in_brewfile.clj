(ns homebrew.missing-in-brewfile
  "lists installed brews not defined in brewfile
   will naturally list a lot of false positives (like dependencies), 
   but better than going through it completely manually"
  (:require [babashka.process :as p]
            [clojure.string :as str]
            [babashka.fs :as fs]
            [clojure.set :as set]))

(defn read-brewfile
  "read ~/.Brewfile, return content as vec"
  []
  (->> (fs/path (fs/home) ".Brewfile") str slurp str/split-lines))

(defn list-installed
  "list installed brew formulas, return as vec"
  []
  (->> (p/shell {:out :string} "brew list -1")
       :out str/trim str/split-lines))

(defn parse-brewfile-formulas
  "attempt to extract brew, tap and cask-names from brewfile contents"
  [brews]
  (->> brews (filter #(re-find #"^(cask|brew|tap)\b" %))
       (map #(second (re-find #"\w+ \"([a-zA-Z0-9\/\-\@\.]+)\"" %)))
       (filter seq)))

(let [installed (->> (list-installed) set)
      in-brewfile (->> (read-brewfile) parse-brewfile-formulas set)
      missing-in-brewfile (set/difference installed in-brewfile)]
  (mapv println (sort missing-in-brewfile)))
