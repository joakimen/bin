(ns containers.get-docker-containers
  "get docker containers from json and return as edn"
  (:require [babashka.process :as p]
            [cheshire.core :as json]
            [clojure.string :as str]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (if (zero? exit)
      (str/trim out)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))))

(defn get-docker-containers []
  (run "docker ps --all --format json"))

(defn parse-json-seq [s]
  (json/parsed-seq (java.io.StringReader. s) true))

(defn -main [& _]
  (->> (get-docker-containers)
       (parse-json-seq)
       prn))

(-main)
