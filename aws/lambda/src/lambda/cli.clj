(ns lambda.cli
  (:require [lambda.core :as core]))

(defn list-functions
  "List lambda functions"
  [_]
  (core/list-functions))
