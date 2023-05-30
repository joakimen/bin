(ns bbfile.cli
  (:require [bbfile.core :as bbfile]))

(defn fuzzy-edit
  "select one or more file with fuzzy completion, open in nvim"
  [_]
  (bbfile/fuzzy-edit))

(defn get-file-dir "select a file with fuzzy completion, return parent dir abspath" [_]
  (bbfile/get-file-dir))
