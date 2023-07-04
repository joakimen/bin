(ns bbfile.cli
  (:require [bbfile.core :as core]))

(defn fuzzy-edit
  "select one or more file with fuzzy completion, open in nvim"
  [_]
  (core/fuzzy-edit))

(defn get-file-dir
  "select a file with fuzzy completion, return parent dir abspath" [_]
  (core/get-file-dir))
