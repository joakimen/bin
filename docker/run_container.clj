(ns docker.run-container
  (:require [babashka.process :as p]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.pprint :as pp]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (if (zero? exit)
      (str/trim out)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))))

(defn list-images []
  (edn/read-string (run "list-images")))

(defn fzfm [v]
  (let [{:keys [out exit]} @(p/process ["fzf" "-m"]
                                       {:in (str/join "\n" v) :err :inherit
                                        :out :string})]
    (if (zero? exit)
      (-> out str/trim str/split-lines)
      (System/exit exit))))

(->> (list-images)
     (mapv #(format "%s:%s" (:Repository %) (:Tag %)))
     (fzfm)
     (mapv #(assoc {} :image % :container-id (run "docker" "run" "-d" %)))
     (pp/print-table))
