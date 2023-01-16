(ns codepipeline.main
  (:require [cheshire.core :as json]
            [com.grzm.awyeah.client.api :as aws]))

(def client (aws/client {:api :codepipeline
                         :region "eu-west-1"}))

(defn get-pipelines []
  (->> (aws/invoke client {:op :ListPipelines}) :pipelines (map :name)))

(defn last-execution [pipelineName]
  (let [execution (-> (aws/invoke client {:op :ListPipelineExecutions
                                          :request {:pipelineName pipelineName
                                                    :maxResults 1}})
                      :pipelineExecutionSummaries
                      first
                      (select-keys [:status :lastUpdateTime]))]
    (conj execution {:pipelineName pipelineName})))

(defn edn-to-json [ednString]
  (json/generate-string ednString {:pretty true
                                   :date-format "YYYY-MM-dd hh:mm:ss"}))

(->> (get-pipelines)
     (map last-execution)
     (edn-to-json)
     (println))

(comment

  (aws/ops client)
  (aws/doc client :GetPipelineState)

  (def client (aws/client {:api :codepipeline
                           :region "eu-west-1"}))
  (def pipelines
    (->> (aws/invoke client {:op :ListPipelines})
         :pipelines (map :name)))

  (def executions (map last-execution pipelines))
;; 
  )
