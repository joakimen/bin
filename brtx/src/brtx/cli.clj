(ns brtx.cli
  (:require [babashka.process :as p]
            [cheshire.core :as json]
            [fzf.core :refer [fzf]]))

(defn- sh [& args]
  (apply println args)
  (:out (apply p/sh args)))

(defn- ->edn [s]
  (json/parse-string s true))

#_{:clj-kondo/ignore [:redefined-var]}
(defn use [{:keys [g global]}]
  (when-let [data (-> (sh "rtx ls --json") ->edn)]
    (when-let [plugin (->> (keys data)
                           (map name)
                           (fzf))]
      (when-let [version (->> (keyword plugin)
                              (get data)
                              (map :version)
                              (fzf))]
        (let [cmd (cond-> ["rtx" "use" (str plugin "@" version)]
                    (or g global) (conj "--global"))]
          (apply sh cmd))))))
