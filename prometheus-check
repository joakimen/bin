#!/usr/bin/env bb
(ns prometheus.client
  (:require [babashka.curl :as curl]
            [cheshire.core :as json]))

;; finds prometheus-targets with a non-"up" health

(defn get-targets [url]
  (filter #(not= (:health %) "up")
          (:activeTargets (:data (json/parse-string (:body (curl/get url)) true)))))

(let [[url] *command-line-args*]
  (doseq [t (get-targets url)] (println
     (:team (:discoveredLabels t))
     (:instance (:labels t))
     (:globalUrl t))))
