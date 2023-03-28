(ns aws.secrets-manager.get-secret
  (:require [babashka.process :as p]
            [cheshire.core :as json]
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

(defn get-secret-value [secret-name]
  (run "aws" "secretsmanager" "get-secret-value"
       "--query" "SecretString"
       "--output" "text"
       "--secret-id" secret-name))

(defn list-secrets []
  (let [secrets (run "aws" "secretsmanager" "list-secrets")]
    (->> (json/parse-string secrets true)
         :SecretList
         (map :Name))))

(->> (list-secrets)
     (str/join "\n")
     (fzf)
     (get-secret-value)
     (println))
