(ns bbgit.lib.sh
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn sh [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (if (zero? exit)
      (str/trim out)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))))
