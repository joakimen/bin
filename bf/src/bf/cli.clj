(ns bf.cli
  (:require [bf.core :as core]))

(defn edit
  "Select one or more file with fuzzy completion, open in nvim"
  [_]
  (core/fuzzy-edit))

(defn path
  "Return the path of a file"
  [_]
  (core/get-file-path))

(defn parent-path
  "Return the path of the parent-dir of a file"
  [_]
  (core/get-file-parent-path))

#_{:clj-kondo/ignore [:redefined-var]}
(defn cat
  "cat a file using fuzzy selection"
  [_]
  (core/fuzzy-cat))

(defn rm
  "rm a file using fuzzy selection"
  [_]
  (core/fuzzy-rm))

(defn git
  "View git history of file using fuzzy selection and lazygit"
  [_]
  (core/fuzzy-git))

(defn dir
  "Select a dir using fuzzy completion"
  [_]
  (core/select-dir))
