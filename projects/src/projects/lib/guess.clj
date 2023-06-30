(ns projects.lib.guess
  (:require [babashka.fs :as fs]))

(defn- detect
  "execute detector-function and return result"
  [project]
  (-> project
      (assoc :files ((:detector project)))
      (dissoc :detector)))

(defn- project-rank
  "return the highest rank of all files in a project"
  [project]
  (:rank (apply max-key :rank (:files project))))

(defn guess-project-type
  "here we define project-types and the ways in which to detect them.
the rank denotes the certainty with which we expect the presence of a 
given file determines the type of the project (a lower rank indicates 
higher confidence). 
e.g. a package.json-file ;; might not indicate it is a node project, 
as some projects use a few things from the node ecosystem alongside
their actual build tool, hence package.json is given a higher rank."
  [repo]
  (let [files-exists? (fn [files] (filter #(fs/readable? (fs/file (fs/expand-home repo) (:file %))) files))
        project-types [{:type :maven
                        :detector #(files-exists? [{:file "pom.xml" :rank 1}])}
                       {:type :clojure
                        :detector #(files-exists? [{:file "bb.edn" :rank 1}
                                                   {:file "deps.edn" :rank 1}])}
                       {:type :go
                        :detector #(files-exists? [{:file "go.mod" :rank 1}])}
                       {:type :node
                        :detector #(files-exists? [{:file "package.json" :rank 10}])}]
        projects-found (->> project-types
                            (map detect) ;; detect files, filter out files that don't exist
                            (filter #(not-empty (:files %)))) ;; filter out project-types with no detected files
        project (->> projects-found
                     (sort #(compare (project-rank %1) (project-rank %2))) ;; reverse, highest rank first
                     first)]
    (or (:type project) :unknown)))

(comment
  (let [repo "~/bin"

        files-exists? (fn [files] (filter #(fs/readable? (fs/file repo (:file %))) files))

        projects [{:type "clojure"
                   :detector #(files-exists? [{:file "deps.edn" :rank 4}
                                              {:file "bb.edn" :rank 2} ;; should win, with the lowest rank
                                              {:file "erg" :rank 9}])} ;; should get filtered out
                  {:type "maven"
                   :detector #(files-exists? [{:file "pom.xml" :rank 7}
                                              {:file "logback.xml" :rank 8}])}]]

    (->> projects
         (map detect) ;; detect files, filter out files that don't exist
         (filter #(not-empty (:files %))) ;; filter out project-types with no detected files
         (sort #(compare (project-rank %1) (project-rank %2)))
         first
         :type))
  ;; => "clojure"
  )
