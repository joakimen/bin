(ns lambda.lib.lambda
  (:require [com.grzm.awyeah.client.api :as aws]))

(defn client [] (aws/client {:api :lambda}))

(defn list-functions [client]
  (:Functions (aws/invoke client {:op :ListFunctions})))

(comment

  (def client (client))

  (list-functions client)

  ;;
  )
