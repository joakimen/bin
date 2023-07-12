(ns brtx.cli
  (:require [babashka.process :as p]
            [cheshire.core :as json]
            [clojure.string :as str]
            [fzf.core :refer [fzf]]))

(defn- sh [& args]
  (apply println args)
  (:out (apply p/sh args)))

(defn- ->edn [s]
  (json/parse-string s true))

(defn set-local [{:keys [g global]}]
  (let [data (-> (sh "rtx ls --json") ->edn)
        plugins (map name (keys data))
        plugin (fzf plugins)
        version (->> plugin keyword (get data) (map :version) fzf)]
    (when (or (str/blank? plugin) (str/blank? version))
      (throw (ex-info "couldn't parse plugin or version data" {:babashka/exit 1})))
    (let [cmd (cond-> ["rtx" "use" (str plugin "@" version)]
                (or g global) (conj "--global"))]
      (apply sh cmd))))
