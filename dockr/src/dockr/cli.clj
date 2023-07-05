(ns dockr.cli
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [dockr.lib.container :as c]
            [dockr.lib.image :as i]
            [fzf.core :refer [fzf]]))

(defn inspect-image
  "Inspect an image"
  [{:keys [edn]}]
  (let [client (i/client)
        images (->> (i/list-images client)
                    (map (comp first :RepoTags)))
        image (fzf {:in images})
        details (i/inspect-image client image)]
    (if edn
      (prn details)
      (println (json/generate-string details {:pretty true})))))

(defn delete-containers
  "Delete one or more stopped containers"
  [_]
  (let [client (c/client)
        stopped-containers (->> (c/list-containers client {:all true})
                                (filter (fn [c] (some #(= (:State c) %) ["exited" "dead"])))
                                (map (comp first :Names)))
        containers (fzf {:in stopped-containers
                         :multi true})]
    (when (empty? containers)
      (throw (ex-info "no containers selected" {:babashka/exit 1})))
    (println "deleting containers")
    (doseq [c containers]
      (println "-" c)
      (c/delete-container client c))))

(defn delete-dangling-images
  "Delete images with status dangling=true"
  [_]
  (let [client (i/client)
        dangling-images (i/list-images client {:filters (json/generate-string {:dangling ["true"]})})]
    (when (empty? dangling-images)
      (throw (ex-info "no dangling images found" {:babashka/exit 1})))
    (println "deleting" (count dangling-images) "dangling image(s)")
    (doseq [{:keys [Id Size RepoTags]} dangling-images]
      (printf "- %s (%s kB) %s\n"
              (subs (second (str/split Id #":")) 0 12)
              (double (/ Size 1000))
              (str/join ", " RepoTags))
      (i/delete-image client Id))))
