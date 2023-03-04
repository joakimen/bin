#!/usr/bin/env bb
(require '[babashka.process :as p]
         '[clojure.string :as str])

;; install asdf-plugins, filtered through fzf

(defn run [cmd]
  (let [res (p/sh cmd)]
    (when (> (:exit res) 0)
      (println (:err res))
      (System/exit (:exit res)))
    (->> res :out str/trim)))

(defn list-languages []
  (->> (run "asdf plugin list")))

(defn list-versions [lang]
  (->> (run (str "asdf list all " lang))))

(defn fzf [s]
  (let [proc (p/process ["fzf" "-m"]
                        {:in s :err :inherit
                         :out :string})]
    (-> @proc :out str/trim)))

(let [lang (fzf (list-languages))
      version (fzf (list-versions lang))]
  (println "installing" lang "version" version)
  (p/shell (format "asdf install %s %s" lang version)))
