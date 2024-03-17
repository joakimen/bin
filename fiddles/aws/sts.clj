(ns sts
  (:require [clojure.pprint :refer [pprint]]
            [cognitect.aws.client.api :as aws]
            [cognitect.aws.credentials :as credentials]))

;;;;;;;;;;;;;;;;;;; default credentials
(def sts (aws/client {:api :sts
                      :region "eu-west-1"}))

(defn get-caller-identity []
  (aws/invoke sts {:op :GetCallerIdentity}))

(get-caller-identity)

;;;;;;;;;;;;;;;;;;;;;; ASSUME ROLE EXAMPLE

;; keychain dump

(defn whoami [creds]
  (let [sts-client (aws/client {:api :sts
                                :region "eu-west-1"
                                :credentials-provider (credentials/basic-credentials-provider creds)})]
    (pprint (aws/invoke sts-client {:op :GetCallerIdentity}))))

;; example
(whoami {:access-key-id "ASIATY00000000000000" :secret-access-key "****************************************"})
{:UserId "AIDAZZZZZZZZZZZZZZZZZ",
 :Account "123412341234",
 :Arn "arn:aws:iam::123412341234:user/my-user"}

(aws/doc sts :AssumeRole)

(defn create-sts-client [user-creds]
  (aws/client {:api :sts
               :region "eu-west-1"
               :credentials-provider (credentials/basic-credentials-provider user-creds)}))

(defn wrap-in-credentials-provider [creds]
  (credentials/cached-credentials-with-auto-refresh
   (reify credentials/CredentialsProvider
     (fetch [_]
       {:aws/access-key-id     (:AccessKeyId creds)
        :aws/secret-access-key (:SecretAccessKey creds)
        :aws/session-token     (:SessionToken creds)
        ::credentials/ttl      (credentials/calculate-ttl creds)}))))
