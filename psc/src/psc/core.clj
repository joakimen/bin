(ns psc.core
  (:require [clojure.string :as str]
            [psc.sh :as psc.sh :refer [ps-axo]]
            [fzf.core :refer [fzf]]))


(defn- parse-process
  "converts a space-delimited process string to kv pairs"
  [p]
  (let [[user pid time & cmd] (str/split p #"\s+")]
    {:user user
     :pid pid
     :time time
     :cmd cmd}))

(defn- get-processes []
  (->> (ps-axo)
       (str/split-lines)
       (map parse-process)))

(defn kill-process []
  (if-let [process (fzf (get-processes))]
    ;; parse string to edn
    (do (println "P:" process ":P")

        (println "selected process" (type process)))
    (println "no process was selected")))
;; (list-processes)
