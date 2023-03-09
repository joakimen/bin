(ns asdf.set-local-version
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn fzf [s]
  (let [res @(p/process ["fzf"]
                        {:in s :err :inherit
                         :out :string})]
    (when (> (:exit res) 0)
      (System/exit (:exit res)))
    (->> res :out str/trim)))

(defn fzfv [v]
  (->> (str/join "\n" v)
       (fzf)
       (str/split-lines)))

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
  (let [versions (->>
                  (run (str "asdf list " lang))
                  (str/split-lines)
                  (mapv #(str/replace % #"[\s\*]" "")))]
    (assoc {} :lang lang :versions versions)))

(let [all-langs (list-languages)
      installed-versions (mapv #(future (list-versions %)) all-langs)
      lang (->> all-langs fzfv first)
      version (->> installed-versions
                   (map deref)
                   (filter #(= (:lang %) lang))
                   first :versions (str/join "\n") fzf)]
  (p/shell "asdf" "local" lang version))
