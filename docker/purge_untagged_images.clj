(ns docker.purge-untagged-images
  (:require [babashka.process :as p]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [taoensso.timbre :as log]))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (if (zero? exit)
      (str/trim out)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))))

(defn -main [& _]
  (let [untagged-images (->> (run "get-docker-images")
                             (edn/read-string)
                             (filter #(or (= "<none>" (:Repository %)) (= "<none>" (:Tag %)))))]

    (when (empty? untagged-images)
      (throw (ex-info "No untagged images found." {:babashka/exit 1})))

    (log/info (format "deleting %d untagged images" (count untagged-images)))
    (doseq [image untagged-images]
      (log/info "Deleting image" (select-keys image [:ID :Size :Repository :Tag]))
      (p/shell "docker rmi" (:ID image)))))

(-main)
