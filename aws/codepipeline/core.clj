(ns codepipeline.core
  (:require [cheshire.core :as json]
            [com.grzm.awyeah.client.api :as aws]))

(defn create-client []
  (aws/client {:api :codepipeline
               :region "eu-west-1"}))

(defn list-pipelines [client]
  (->> (aws/invoke client {:op :ListPipelines}) :pipelines (map :name)))

(defn last-execution [client pipelineName]
  (let [execution (-> (aws/invoke client {:op :ListPipelineExecutions
                                          :request {:pipelineName pipelineName
                                                    :maxResults 1}})
                      :pipelineExecutionSummaries
                      first
                      (select-keys [:status :lastUpdateTime]))]
    (conj execution {:pipelineName pipelineName})))

(defn edn-to-json [ednData]
  (json/generate-string ednData {:pretty true
                                 :date-format "YYYY-MM-dd hh:mm:ss"}))

(defn transform-stage [stage]
  {:name (:stageName stage)
   :status (-> stage :latestExecution :status)})

(defn list-stages [client pipelineName]
;; needs timestamps to be useful
  (let [pipelineState (aws/invoke client {:op :GetPipelineState
                                          :request {:name pipelineName}})
        stages (->> pipelineState :stageStates (map transform-stage))]
    {:pipeline pipelineName
     :stages stages}))

(let [client (create-client)
      pipelines (list-pipelines client)]
  (->> pipelines
      ;;  (pmap #(list-stages client %))
       (pmap #(last-execution client %))
       (edn-to-json)
       (doall)
       (println)))

(comment
  (aws/ops client)
  (aws/doc client :GetPipelineState)

  (def client (create-client))

  (aws/invoke client {:op :ListPipelines})
  (list-pipelines client)
  (list-stages client "pl-123")

  (->>
   (aws/invoke client {:op :ListPipelineExecutions
                       :request {:pipelineName "pl-123"
                                 :maxResults 3}})
   :pipelineExecutionSummaries
  ;;  (map #(select-keys % [:status]))
  ;;  
   )


  (defn transform-stage [stage]
    {:name (:stageName stage)
     :status (-> stage :latestExecution :status)})

  (->> (aws/invoke client {:op :GetPipelineState
                           :request {:name "pl-123"}})
       :stageStates
       (map transform-stage))


;; 
  )
