(ns lambda.cli
  (:require [lambda.core :as core]))

(defn list
  "List lambda functions"
  [_]
  (core/list-functions))
