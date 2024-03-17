(ns s3
  (:require [cognitect.aws.client.api :as aws]))

(defn list-all-objects
  [client bucket]
  (loop [cur-files [] token nil]
    (let [response (aws/invoke client {:op :ListObjectsV2
                                       :request {:Bucket bucket
                                                 :ContinuationToken token}})
          new-files (:Contents response)
          next-token (:NextContinuationToken response)
          total-files (concat cur-files new-files)]
      (if next-token
        (recur total-files next-token)
        total-files))))
