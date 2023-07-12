(ns projects.config
  (:require [babashka.fs :as fs]
            [clojure.edn :as edn]
            [clojure.spec.alpha :as s]))

(defn- die [& args]
  (throw (ex-info (apply str args) {:babashka/exit 1})))

(def config-file
  (str (fs/path (fs/xdg-config-home) "projects.edn")))

(defn- dir? [dir]
  (boolean (and (not-empty dir)
                (fs/directory? (fs/expand-home dir)))))

(defn- slurp-edn [file]
  (-> file slurp edn/read-string))

(s/def ::clone-dir dir?)
(s/def ::roots (s/coll-of dir? :min-count 1))
(s/def ::ignores (s/coll-of string?))

(s/def ::max-depth pos?)
(s/def ::preview-cmd string?)

(s/def ::settings (s/keys :opt-un [::max-depth ::preview-cmd]))

(s/def ::config
  (s/keys
   :req-un [::clone-dir ::roots]
   :opt-un [::settings ::ignores]))

(defn parse-config
  "parse contents of configuration file"
  [{:keys [projects]}]
  {:pre [(s/valid? ::config projects)]}
  (let [{:keys [roots clone-dir ignores settings]} projects]
    (when (empty? roots)
      (die "error: no project-entries defined in config-file: " config-file))
    {:roots (map (comp str fs/expand-home) roots)
     :clone-dir (-> clone-dir fs/expand-home str)
     :ignores (into [] ignores)
     :settings settings}))

(defn read-config []
  (-> config-file slurp-edn parse-config))
