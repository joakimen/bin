#!/usr/bin/env bb
(ns file.list-filedirs
  "list all files recursively from cwd, filter with fzf. return parent-dir of selected file"
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.string :as str]))

(let [file (-> (p/process "fd" "-t" "file")
               (p/process {:err :inherit} "fzf") deref :out slurp)]
  (when (not (str/blank? file))
    (println (-> file fs/absolutize fs/parent str))))
