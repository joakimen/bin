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

;; Example steps
;; 1: create client with basic user credentials
;; 2: assume role with the client, along with mfa serial and mfa token
;; 3: create s3 client with assumed role credentials
;; 4. list buckets using s3 client
(let [
      ;; load from 1password or somethin
      mfa-token-code "577170" 

      ;; load with aero
      mfa-serial-number "arn:aws:iam::123412341234:mfa/jol"
      user-creds {:access-key-id "AKIATY00000000000000"
                  :secret-access-key "****************************************"}
      assume-role-arn "arn:aws:iam::123412341234:role/OtherRole"
      sts-client (create-sts-client user-creds)
      assumed-role-creds (assume-role {:sts-client sts-client
                                       :role-arn assume-role-arn
                                       :mfa-serial mfa-serial-number
                                       :mfa-token mfa-token-code
                                       :session-prefix "my-session"})
      credentials-provider (wrap-in-credentials-provider assumed-role-creds)
      s3-client (aws/client {:api :s3
                             :region "eu-west-1"
                             :credentials-provider credentials-provider})]

  ;; list buckets using assumed role credentials 
  (def assumed-role-creds assumed-role-creds)
  (aws/invoke s3-client {:op :ListBuckets}))
