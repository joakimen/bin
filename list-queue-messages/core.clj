(require '[com.grzm.awyeah.client.api :as aws]
         '[clojure.walk :refer [keywordize-keys]]
         '[clojure.set :as set]
         '[cheshire.core :as json])

;; list sqs queues and their visible/hidden messages

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
        messages (get-queue-attributes client queueUrl attributeNames)]
    {:queueUrl queueUrl
     :messages (set/rename-keys messages {:ApproximateNumberOfMessages :visible
                                          :ApproximateNumberOfMessagesNotVisible :invisible})}))

(defn to-json [data]
  (json/generate-string data {:pretty true}))

(let [client (create-client)
      queues (list-queues client)]
  (->> queues
       (map #(future (get-queue-messages client %)))
       (map deref)
       (to-json)
       (println)))

(comment
  (def client (create-client))

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
