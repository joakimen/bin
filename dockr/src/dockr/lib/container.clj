(ns dockr.lib.container
  (:require [contajners.core :as c]
            [dockr.config :refer [base-config]]
            [cheshire.core :as json]))

(defn client []
  (c/client (conj base-config [:category :containers])))

(defn list-containers
  ([client] (list-containers client {}))
  ([client params]
   (c/invoke client {:op :ContainerList
                     :params params})))

(defn delete-container [client container]
  (c/invoke client {:op :ContainerDelete
                    :params {:id container}}))

(comment
  (list-containers {:all true
                    :filters (json/generate-string {:status ["exited" "dead"]})})
  ;;
  )
