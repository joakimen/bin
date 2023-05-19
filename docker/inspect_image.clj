(ns docker.inspect-image
  (:require [babashka.process :as p]
            [clojure.string :as str]
            [clojure.edn :as edn]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (if (zero? exit)
      (str/trim out)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))))

(defn fzf [v]
  (let [{:keys [out exit]} @(p/process ["fzf"]
                                       {:in (str/join "\n" v) :err :inherit
                                        :out :string})]
    (if (zero? exit)
      (str/trim out)
      (System/exit exit))))

(defn list-images []
  (edn/read-string (run "list-images")))

(defn inspect-image [image]
  (run "docker" "inspect" image))

(->> (list-images)
     (map #(str (:Repository %) ":" (:Tag %)))
     (fzf)
     (inspect-image)
     (println))
