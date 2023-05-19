(ns docker.delete-containers
  (:require [babashka.process :as p]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(defn fzfm [v]
  (let [{:keys [out exit]} @(p/process ["fzf" "-m"]
                                       {:in (str/join "\n" v) :err :inherit
                                        :out :string})]
    (if (zero? exit)
      (-> out str/trim str/split-lines)
      (System/exit exit))))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (if (zero? exit)
      (str/trim out)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))))

(defn list-stopped-containers []
  (edn/read-string (run "list-stopped-containers")))

(defn delete-container [c]
  (let [cmd ["docker" "rm" c]]
    (apply println cmd)
    (apply p/shell cmd)))

(defn -main [& _]
  (let [stopped-containers (list-stopped-containers)
        containers-to-delete (->> stopped-containers (mapv :Names) fzfm)]
    (when (empty? containers-to-delete)
      (throw (ex-info "no containers selected" {:babashka/exit 1})))
    (mapv delete-container containers-to-delete)))

(-main)
