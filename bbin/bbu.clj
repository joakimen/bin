(ns bbin.bbu "uninstall a bbin script"
    (:require [babashka.process :as p]
              [clojure.edn :as edn]
              [clojure.string :as str]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (str/trim out)))

(defn fzf [s]
  (let [{:keys [out exit]} @(p/process ["fzf" "-m"]
                                       {:in s :err :inherit
                                        :out :string})]
    (when-not (zero? exit)
      (System/exit exit))
    (str/trim out)))

(let [out (run "bbin" "ls" "--edn")
      script-names (->> out edn/read-string keys)
      script-name (->> script-names (str/join "\n") fzf)]

  (when-not (str/blank? script-name)
    (println "uninstalling" script-name)
    (p/shell "bbin" "uninstall" script-name)))
