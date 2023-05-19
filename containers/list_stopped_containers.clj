(ns containers.list-stopped-containers
  (:require [babashka.process :as p]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (if (zero? exit)
      (str/trim out)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))))

(defn list-containers []
  (edn/read-string (run "list-containers")))

(defn container-startable? [container]
  (some #(= (:State container) %) ["running" "exited"]))

(->>
 (list-containers)
 (filter container-startable?)
 (prn))
