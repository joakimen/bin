#!/usr/bin/env bb
(ns git.guess-project
  (:require [babashka.process :refer [sh]]
            [babashka.fs :as fs]
            [clojure.string :as str]))

(defn run
  "run shell cmd"
  [cmd]
  (let [result (sh cmd)]
    (when (> (:exit result) 0)
      (->> result :err str/trim println)
      (System/exit (:exit result)))
    (->> result :out str/trim)))

(defn repo-file-exists? [repo]
  (fn [file] (fs/exists? (fs/path repo file))))

(defn detect
  "execute detector-function and return result"
  [project]
  (-> project
      (assoc :found ((:detector project))) ;; bad but works
      (dissoc :detector)))

(let [repo (run "git rev-parse --show-toplevel")
      file-exists? (repo-file-exists? repo)

      ;; here we define project-types and the ways in which to detect them.
      ;; the rank denotes the certainty with which we expect the presence of a 
      ;; given file will determine the type of project (lower is better). 
      ;; e.g. a package.json-file ;; might not indicate it is a node project, 
      ;; as some projects use a few things from the node ecosystem alongside
      ;; their actual build tool, hence package.json is given a higher rank.
      project-types [{:type "maven"
                      :detector (fn [] (file-exists? "pom.xml"))
                      :rank 1}
                     {:type "go"
                      :detector (fn [] (file-exists? "go.mod"))
                      :rank 1}
                     {:type "babashka"
                      :detector (fn [] (file-exists? "bb.edn"))
                      :rank 1}
                     {:type "node"
                      :detector (fn [] (file-exists? "package.json"))
                      :rank 10}]
      projects-found (->> project-types
                          (map #(future (detect %)))
                          (map deref)
                          (filter #(= (:found %) true)))
      project (->> projects-found
                   (sort-by :weight #(compare %2 %1)) ;; reverse
                   first)]

  (when (nil? project)
    (println "could not determine project-type for:" (str (fs/cwd)))
    (println "supported project-types:" (str/join ", " (->> project-types (map :type))))
    (System/exit 1))

  (println (:type project)))
