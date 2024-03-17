(ns psc.cli
  [:require [psc.core :as core]])

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn kill [_]
  (core/kill-process))
