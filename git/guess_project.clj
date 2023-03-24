#!/usr/bin/env bb
(ns git.guess-project
  (:require [babashka.process :refer [sh]]
            [babashka.fs :as fs]
            [clojure.string :as str]))

(defn run
  "run shell cmd" [cmd]
  (let [{:keys [out err exit]} (sh {:out :string} cmd)]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (str/trim out)))

(def repo (run "git rev-parse --show-toplevel"))

(defn files-exists?  [files]
  (fn [] (filter #(fs/exists? (fs/path repo (:file %))) files)))

(defn detect
  "execute detector-function and return result"
  [project]
  (-> project
      (assoc :files ((:detector project)))
      (dissoc :detector)))

(defn project-rank
  "return the highest rank of all files in a project"
  [project]
  (:rank (apply max-key :rank (:files project))))

;; here we define project-types and the ways in which to detect them.
;; the rank denotes the certainty with which we expect the presence of a 
;; given file will determine the type of project (lower is better). 
;; e.g. a package.json-file ;; might not indicate it is a node project, 
;; as some projects use a few things from the node ecosystem alongside
;; their actual build tool, hence package.json is given a higher rank.
;; 
;; if we update file deteetion to check for multiple files per lang,
;; the files should then have their own separate rank, and when considering
;; the files that were actually found, the one with the highest rank must be
;; considered when guessing project type
(let [project-types [{:type "maven"
                      :detector (files-exists? [{:file "pom.xml" :rank 1}])}
                     {:type "clojure"
                      :detector (files-exists? [{:file "bb.edn" :rank 1}
                                                {:file "deps.edn" :rank 1}])}
                     {:type "go"
                      :detector (files-exists? [{:file "go.mod" :rank 1}])}
                     {:type "node"
                      :detector (files-exists? [{:file "package.json" :rank 10}])}]
      projects-found (->> project-types
                          (mapv detect) ;; detect files, filter out files that don't exist
                          (filter #(not-empty (:files %)))) ;; filter out project-types with no detected files
      project (->> projects-found
                   (sort #(compare (project-rank %1) (project-rank %2))) ;; reverse, highest rank first
                   first)]
  (when (nil? project)
    (let [err-msg (str "could not determine project-type for: " (fs/cwd) ", supported project-types: " (str/join ", " (->> project-types (map :type))))]
      (throw (ex-info err-msg {:babashka/exit 1}))))
  (println (:type project)))


(comment
  (let [projects [{:type "clojure"
                   :detector (files-exists? [{:file "deps.edn" :rank 4}
                                             {:file "bb.edn" :rank 2} ;; should win, with the lowest rank
                                             {:file "fakefile.txt" :rank 9}])} ;; should get filtered out
                  {:type "maven"
                   :detector (files-exists? [{:file "pom.xml" :rank 7}
                                             {:file "logback.xml" :rank 8}])}]]

    (->> projects
         (map detect) ;; detect files, filter out files that don't exist
         (filter #(not-empty (:files %))) ;; filter out project-types with no detected files
         (sort #(compare (project-rank %1) (project-rank %2)))
         first
         :type))
  ;; => "clojure"
  )
