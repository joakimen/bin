(ns aws.sqs.receive-messages
  (:require [babashka.process :as p]
            [cheshire.core :as json]
            [clojure.string :as str]))

;; receive messages from a queue. queue is selected using fzf

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (str/trim out)))

(defn receive-from-queue [queue-name]
  (run "aws" "sqs" "receive-message" "--queue-url"  queue-name
       "--attribute-names" "All" "--message-attribute-names" "All" "--max-number-of-messages" 10))

(defn parse-json [s]
  (json/parse-string s true))

(defn list-sqs-queues []
  (-> (run "aws sqs list-queues") parse-json :QueueUrls))

(defn fzf [s]
  (let [{:keys [out exit]} @(p/process ["fzf" "-m"]
                                       {:in s :err :inherit
                                        :out :string})]
    (when-not (zero? exit)
      (System/exit exit))
    (str/trim out)))

(let [queue (->> (list-sqs-queues) (str/join "\n") fzf)]
  (when (str/blank? queue)
    (throw (ex-info "No queue selected" {:babashka/exit 1})))
  (if-let [messages (receive-from-queue queue)]
    (p/shell {:in messages} "jq")
    (println "No messages in queue" queue)))
