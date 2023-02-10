#!/usr/bin/env bb
(require '[clojure.java.shell :refer [sh]]
         '[cheshire.core :as json]
         '[clojure.string :as str])

(defn parse [s]
  (json/parse-string s true))

;; TODO: use new bb-friendly aws sdk instead
(->> (sh "aws" "lambda" "list-functions")
     :out parse :Functions
     (map :FunctionName)
     (sort)
     (str/join "\n")
     println)
