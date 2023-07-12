(ns projects.cli
  (:require [projects.core :as core]))

(defn guess
  "Guess which type of project this is"
  [{:keys [edn]}]
  (let [proj-type (core/guess-type)]
    (if edn
      (prn proj-type)
      (println (name proj-type)))))


#_{:clj-kondo/ignore [:redefined-var]}
(defn list
  "List all tracked projects"
  [{:keys [edn]}]
  (let [res (core/list-projects)]
    (if edn
      (prn res)
      (run! println res))))

(defn delete
  "Delete one or more local projects"
  [_]
  (core/delete-projects))
