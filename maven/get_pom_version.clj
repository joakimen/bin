#!/usr/bin/env bjb
(ns maven.get-pom-version
  (:require [clojure.data.xml :as xml]
            [clojure.string :as str]
            [babashka.fs :as fs]))

(defn tag-name? [tag tname]
  (some-> tag :tag name #{tname}))

(defn tag-content-str [tag]
  (->> tag :content (filter string?) (str/join "")))

(defn get-version [path]
  (->> (slurp path)
       xml/parse-str
       xml-seq
       (filter #(tag-name? % "version"))
       first
       tag-content-str))

(let [pom (or (first *command-line-args*) "pom.xml")]
  (when (not (fs/exists? pom))
    (println (str pom ": No such file or directory"))
    (System/exit 1))
  (-> pom get-version println))
