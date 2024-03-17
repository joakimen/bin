(ns psc.sh
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn- $ [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (if-not (zero? exit)
      (throw (ex-info (str "error running command: " err)
                      {:cmd args :exit exit :err err :babashka/exit 1}))
      (str/trim out))))

(defn ps-axo []
  ($ "ps" "axo" "user=,pid=,time=,args="))

(comment
  (ps-axo)
  :rfc)
