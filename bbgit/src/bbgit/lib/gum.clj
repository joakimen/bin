(ns bbgit.lib.gum
  (:require [bblgum.core :as b]))

(defn confirm []
  (:result (b/gum :confirm :as :bool)))
