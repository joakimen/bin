(ns docker.list-images
  (:require [babashka.process :as p]
            [cheshire.core :as json]
            [clojure.string :as str]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (if (zero? exit)
      (str/trim out)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))))

(defn parse-json-seq [s]
  (json/parsed-seq (java.io.StringReader. s) true))

(defn get-docker-images []
  (run "docker images --format json"))

(defn -main [& _]
  (->> (get-docker-images)
       (parse-json-seq)
       prn))

(-main)
