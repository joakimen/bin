(ns containers.start-containers
  (:require [babashka.process :as p]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (if (zero? exit)
      (str/trim out)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))))


(defn fzfm [v]
  (let [{:keys [out exit]} @(p/process ["fzf" "-m"]
                                       {:in (str/join "\n" v) :err :inherit
                                        :out :string})]
    (if (zero? exit)
      (-> out str/trim str/split-lines)
      (System/exit exit))))


(defn list-startable-containers []
  (edn/read-string (run "list-stopped-containers")))

(->> (list-startable-containers)
     (mapv :Names)
     (fzfm)
     (pmap #(p/shell "docker" "start" %))
     (doall))
