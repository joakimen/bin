(ns bmise.cli
  (:require [babashka.process :as p]
            [cheshire.core :as json]
            [clojure.string :as str]
            [fzf.core :refer [fzf]]))

(defn- sh [& args]
  (apply println args)
  (:out (apply p/sh args)))

(defn- ->edn [s]
  (json/parse-string s true))

#_{:clj-kondo/ignore [:redefined-var]}
(defn use [{:keys [g global]}]
  (when-let [data (-> (sh "mise ls --json") ->edn)]
    (when-let [plugin (->> (keys data)
                           (map name)
                           (fzf))]
      (when-let [version (->> (keyword plugin)
                              (get data)
                              (map :version)
                              (fzf))]
        (let [cmd (cond-> ["mise" "use" (str plugin "@" version)]
                    (or g global) (conj "--global"))]
          (apply sh cmd))))))

(defn i [_]
  (when-let [lang (-> (sh "mise plugin ls --core --user") str/split-lines fzf)]
    (when-let [version (-> (sh "mise" "ls-remote" lang) str/split-lines fzf)]
      (let [cmd ["mise" "install" (str lang "@" version)]]
        (apply p/shell cmd)))))

(defn rm [_]
  (when-let [lang (-> (sh "mise ls --json --keys") str/split-lines fzf)]
    (when-let [version (-> (sh "mise" "ls" lang) str/split-lines fzf)]
      (let [cmd ["mise" "uninstall" (str lang "@" version)]]
        (apply p/shell cmd)))))

(comment
  (install nil)

  (->> (sh "mise ls --json")
       ->edn
       (map #(p))))

  ;; map over lang keys (:java, :bun, ..)
  ;; map over versions
  ;; flatmap

  ;; 

