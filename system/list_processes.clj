(ns system.list-processes
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn parse-process [s]
  (let [[user pid time & cmd] (str/split s #"\s+")]
    {:user user
     :pid pid
     :time time
     :cmd cmd}))

(defn ps []
  (->> (p/sh "ps" "axo" "user=,pid=,time=,args=")
       :out
       str/split-lines
       (mapv parse-process)))

(prn (ps))
