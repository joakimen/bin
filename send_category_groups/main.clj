(ns send_category_groups.main
  (:require [babashka.fs :as fs]
            [cheshire.core :as json]
            [clojure.data.csv :as csv]
            [com.grzm.awyeah.client.api :as aws]
            [taoensso.timbre :as log]))

(defn create-client []
  (aws/client {:api :sqs
               :region "eu-west-1"}))

(def message-attributes
  {"breadcrumbId" {:DataType "String"
                   :StringValue "f60441c7-e826-460f-97fb-8b4bb438bea1"}
   "entity" {:DataType "String"
             :StringValue "category-group"}
   "compression" {:DataType "String"
                  :StringValue "none"}})

(defn send-sqs-message [client queue-url message-body message-attributes]
  (log/info (aws/invoke client {:op :SendMessage
                                :request
                                {:QueueUrl queue-url
                                 :MessageBody message-body
                                 :MessageAttributes message-attributes}})))

(def queue-url "https://sqs.eu-west-1.amazonaws.com/464824668216/europris-staging-fiftytwo-pos-adapter-master-ingress")
(def csv-file (str (fs/file "/Users/joakim/dev/data/liflig/EUR-1945/Result_23-valid-groupids-working.csv")))
(def groupid-format #"[a-zA-Z]\d{4}")

(defn to-core-event [[groupId groupName]]
  {:status "UPDATED"
   :data {:class ["category-group"]
          :properties {:groupId groupId
                       :groupName groupName}}
   :modified (str (java.time.Instant/now))
   :eventId (str (java.util.UUID/randomUUID))})

(comment

  (let [messages (->> csv-file slurp csv/read-csv
                      (filter #(re-matches groupid-format (first %)))
                      (mapv to-core-event))]
    (println "sending" (count messages) "category groups to queue" queue-url)
    (mapv #(send-sqs-message client queue-url (json/encode %) message-attributes) messages))

  (def client (create-client))
  (aws/validate-requests client true)
  (aws/doc client :SendMessage)

  ;;
  )
