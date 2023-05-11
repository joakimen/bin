(ns aws.sqs.list-queue-messages.main
  (:require [babashka.cli :as cli]
            [clojure.string :as str]
            [clojure.walk :refer [keywordize-keys]]
            [com.grzm.awyeah.client.api :as aws]
            [doric.core :as doric]))

;; list sqs queues and their visible/hidden messages
;; - pass in -a/--all to list empty queues

(defn create-client []
  (aws/client {:api :sqs
               :region "eu-west-1"}))

(defn list-queues [client]
  (:QueueUrls (aws/invoke client {:op :ListQueues})))

(defn get-queue-attributes
  "list visible and invisible messages on a given queue"
  [client queueUrl attributeNames]
  (-> (aws/invoke client {:op :GetQueueAttributes
                          :request
                          {:QueueUrl queueUrl
                           :AttributeNames attributeNames}})
      :Attributes keywordize-keys))

(defn get-queue-messages
  "list visible and invisible messages on a given queue"
  [client queueUrl]
  (let [attributeNames ["ApproximateNumberOfMessages"
                        "ApproximateNumberOfMessagesNotVisible"]
        {:keys [ApproximateNumberOfMessages ApproximateNumberOfMessagesNotVisible]}
        (get-queue-attributes client queueUrl attributeNames)]
    {:visible (Integer/parseInt ApproximateNumberOfMessages)
     :invisible (Integer/parseInt ApproximateNumberOfMessagesNotVisible)
     :queueName (str/replace queueUrl #"https://sqs.*\.amazonaws.com/\d{12}/" "")}))

(def rules
  {:alias {:a :all}
   :coerce {:all :boolean}
   :exec-args {:all false}
   :validate {:all boolean?}})

(let [args (cli/parse-opts *command-line-args* rules)
      show-empty (:all args) ;; if true, also print empty queues
      client (create-client)
      queues (list-queues client)
      messages (pmap #(get-queue-messages client %) queues)
      filtered (if show-empty messages (filter #(or (> (:visible %) 0) (> (:invisible %) 0)) messages))]

  (->> filtered
       (sort-by :queueName)
       (doric/table [:queueName :visible :invisible])
       (println)))

(comment

  (def client (create-client))
  (def queues (list-queues client))


  (let [messages (pmap #(get-queue-messages client %) queues)]
    (->> messages
         (sort-by (juxt :visible :invisible))
         (doric/table)
         (println)))

  (aws/doc client :GetQueueAttributes)

  (-> (aws/ops client) keys sort)


  (-> (aws/invoke client {:op :GetQueueAttributes
                          :request
                          {:QueueUrl "https://sqs.eu-west-1.amazonaws.com/4643458345/my-clj-queue"
                           :AttributeNames
                           ["ApproximateNumberOfMessages"
                            "ApproximateNumberOfMessagesNotVisible"]}})
      :Attributes keywordize-keys)

  (:QueueUrls (aws/invoke client {:op :ListQueues}))
;;
  )
