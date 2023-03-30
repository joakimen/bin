(ns aws.ssm.get-parameter
  (:require [babashka.process :as p]
            [cheshire.core :as json]
            [clojure.string :as str]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (str/trim out)))

(defn fzf [s]
  (let [{:keys [out exit]} @(p/process ["fzf"]
                                       {:in s :err :inherit
                                        :out :string})]
    (when-not (zero? exit)
      (System/exit exit))
    (str/trim out)))

(defn get-params []
  (let [resp (run "aws" "ssm" "describe-parameters")]
    (->> (json/parse-string resp true)
         :Parameters
         (map :Name))))

(defn get-param [name]
  (let [resp (run "aws" "ssm" "get-parameter" "--name" name)]
    (->> (json/parse-string resp true)
         :Parameter :Value)))

(->> (get-params)
     (str/join "\n")
     (fzf)
     (get-param)
     (println))
