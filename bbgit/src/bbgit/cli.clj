(ns bbgit.cli
  (:require [bbgit.core :as core]))

(defn switch-branch "Switch to another branch" [_]
  (core/switch-branch))

(defn revert-commit "Revert a single commit" [_]
  (core/revert-commit))

(defn checkout-pull-request "Checkout a pull-request (requires gh)" [_]
  (core/checkout-pull-request))

(defn checkout-commit "Checkout a commit" [_]
  (core/checkout-commit))

(defn list-branches "List branches in edn format" [_]
  (core/list-branches))

(defn delete-branches "Delete one or more brances" [_]
  (core/delete-branches))
