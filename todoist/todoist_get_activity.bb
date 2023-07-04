(ns todoist.todoist-get-activity
  (:require [babashka.http-client :as http]
            [cheshire.core :as json]
            [clojure.string :as str]))

(import (java.time.format DateTimeFormatter)
        (java.time OffsetDateTime))

;; Lists all added/completed todoist-tasks last 24 hours
;; Expects env var $TODOIST_TOKEN


(defn getenv
  "get env var, err-exit if var is unset"
  [name]
  (or (System/getenv name)
      (throw (ex-info (str "env var " name " is unset") {:babashka/exit 1}))))

(defn query-todoist
  "fetch activity-log from todoist"
  []
  (let [bearer-token (getenv "TODOIST_TOKEN")]
    (-> (http/get "https://api.todoist.com/sync/v9/activity/get"
                  {:headers
                   {"Authorization" (str "Bearer " bearer-token)}})
        :body
        (json/parse-string true))))

(defn date-between?
  [date start end]
  (and (. date (OffsetDateTime/isAfter start))
       (. date (OffsetDateTime/isBefore end))))

(def today (OffsetDateTime/now))
(def yesterday (. today OffsetDateTime/minusDays 1))

(defn happened-in-last-24-hours
  "return true if instant i is within the last 24 hours"
  [{:keys [event_date]}]
  ;; (let [date (OffsetDateTime/parse event_date)]
  (date-between? event_date yesterday today))

(defn format-event [{:keys [event_date event_type extra_data]}]
  (let [date-formatter (DateTimeFormatter/ofPattern "yyyy-MM-dd HH:mm")
        date-string (.format event_date date-formatter)
        title (:content extra_data)]
    (format "%s [%s] %s" date-string event_type title)))

;; (def resp (query-todoist))
;; (->> resp
(->> (query-todoist)
     :events
    ;;  replace date-strings with date-objects for comparison
     (map (fn [x] (update x :event_date #(OffsetDateTime/parse %))))
     (filter happened-in-last-24-hours)
     (filter (fn [e] (some #(= (:event_type e) %) ["added" "completed"])))
     (mapv format-event)
     (str/join "\n")
     (println))
