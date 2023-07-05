(ns dockr.lib.image
  (:require [contajners.core :as c]
            [dockr.config :refer [base-config]]))

(defn client []
  (c/client (merge base-config {:category :images})))

(defn list-images
  ([client] (list-images client {}))
  ([client params]
   (c/invoke client {:op :ImageList
                     :params params})))

(defn inspect-image [client image]
  (c/invoke client {:op :ImageInspect
                    :params {:name image}}))

(defn delete-image [client image]
  (c/invoke client {:op :ImageDelete
                    :params {:name image}}))
