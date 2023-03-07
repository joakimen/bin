#!/usr/bin/env bb
(require '[babashka.process :as p]
         '[clojure.string :as str])

;; install asdf-plugins, filtered through fzf

(defn run [cmd]
  (let [res (p/sh cmd)]
    (when (> (:exit res) 0)
      (->> res :err str/trim println)
      (System/exit (:exit res)))
    (->> res :out str/trim)))

(defn list-languages []
  (->> (run "asdf plugin list")
       (str/split-lines)))

(defn list-versions [lang]
  (->> (run (str "asdf list all " lang))
       (str/split-lines)
       (assoc {} :lang lang :versions)))

(defn fzf [s]
  (let [res @(p/process ["fzf" "-m"]
                        {:in s :err :inherit
                         :out :string})]
    (when (> (:exit res) 0)
      (System/exit (:exit res)))
    (->> res :out str/trim)))

(let [all-langs (list-languages)
      all-versions (mapv #(future (list-versions %)) all-langs)
      lang (->> all-langs (str/join "\n") fzf)
      version (->> all-versions (map deref) (filter #(= (:lang %) lang)) first :versions (str/join "\n") fzf)]
  (println "Installing" lang version)
  (p/shell "asdf" "install" lang version))
