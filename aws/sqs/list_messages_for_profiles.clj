(ns aws.sqs.list-messages-for-profiles
  "lists sqs messages for all profiles provided through stdin
   
   1) list all messages for current env (or all envs? would integrate aws-vault..)
   2) sort by number of messages
   3) pretty-print each queue on single row
   "
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.string :as str]
            [cheshire.core :as json]))

(let [required ["aws-vault" "aws"]
      missing (filterv #(not (fs/which %)) required)]
  (when (not-empty missing)
    (println "missing binaries:" missing)
    (System/exit 1)))

(defn parse-json [s]
  (json/parse-string s true))

(defn list-profile-messages [profile]
  (->> (p/shell {:out :string} "aws-vault" "exec" profile "--" "list-queue-messages")
       :out parse-json))

(defn prettify [row]
  (format "%s (%s in-flight) %s"
          (-> row :messages :visible)
          (-> row :messages :invisible)
          (second (re-find #"https://sqs.*amazonaws\.com/\d+/(.*)$" (:queueUrl row)))))


(let [profiles (->> *in* slurp str/trim str/split-lines)]
  (->> profiles
       (filter #(not (str/blank? %)))
       (pmap list-profile-messages) ;; print inside here maybe? hm
       (mapv println)))



(comment

  ;; need to separate output by account... e.g. 
  ;; 
  ;; # ex-dev-profile
  ;; 1 (2 in-flight) ...  
  ;;
  ;; # ex-staging-profile 
  ;; 6 (0 in-flight) ...

  (def p (list-profile-messages "europris-dtp-dev-developer"))

  (->> p
       (map prettify)
       (mapv println)))






;; 1. read profiles from stdin
;; 2. convert them to a vector
;; 3. pmap them to "aws-vault exec $profile -- list-queue-messages
