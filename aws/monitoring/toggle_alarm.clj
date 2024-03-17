(require '[bblgum.core :as b]
         '[clojure.string :as str]
         '[com.grzm.awyeah.client.api :as aws]
         '[clojure.edn :as edn])

(def cloudwatch (aws/client {:api :monitoring}))

(defn list-alarm-states []
  (let [resp (aws/invoke cloudwatch {:op :DescribeAlarms})]
    (when (contains? resp :cognitect.anomalies/category)
      (throw (ex-info (str "aws command failed: " (:Error resp)) {:babashka/exit 1 :err (:Error resp)})))
    (->> (:MetricAlarms resp)
         (map #(select-keys % [:AlarmName :StateValue])))))

(defn determine-new-state [state]
  (if (= state "OK")
    "ALARM"
    "OK"))

(defn gum-filter [alarms]
  (let [{:keys [result]} (b/gum :filter
                                :in (str/join "\n" alarms)
                                :placeholder "Choose alarm to toggle")]
    (if (seq result)
      (-> result first edn/read-string)
      (throw (ex-info "no alarm selected" {:babashka/exit 1})))))

(defn gum-input []
  (let [{:keys [result]} (b/gum :input :placeholder "State reason")]
    (or (first result) "testing")))

(defn gum-heading [message]
  (let [gum-resp (b/gum :style :in message :border "normal" :padding "0 1" :border-foreground "212")]
    (->> gum-resp :result (run! println))))

(defn gum-confirm []
  (= 0 (:status (b/gum :confirm))))

(defn set-alarm-state [alarm state reason]
  (let [resp (aws/invoke cloudwatch {:op :SetAlarmState
                                     :request {:AlarmName alarm
                                               :StateValue state
                                               :StateReason reason}})]
    (if (contains? resp :cognitect.anomalies/category)
      (throw (ex-info (str "An error occurred while changing alarm state: " (:Error resp)) {:babashka/exit 1 :err (:Error resp)}))
      (println "Alarm state changed successfully."))))

(defn -main [& _]

  (gum-heading "Toggling alarm state")
  (let [alarm (gum-filter (list-alarm-states))
        alarm-with-new-state (assoc alarm :StateValue (determine-new-state (:StateValue alarm)))
        reason (gum-input)]
    (println "Alarm:" (:AlarmName alarm))
    (println "Old state:" (:StateValue alarm))
    (println "New state:" (:StateValue alarm-with-new-state))
    (println "Reason:" reason)

    (if (gum-confirm)
      (set-alarm-state (:AlarmName alarm-with-new-state)
                       (:StateValue alarm-with-new-state)
                       reason)
      (println "Aborted."))))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))

(comment
  (def alarm (gum-filter (list-alarm-states)))
  alarm
  (def message (gum-input))
  message

  (def error-ex {:Error                                                        ;; provided by AWS
                 {:Message "The specified key does not exist."                 ;; provided by AWS
                  :Code "NoSuchKey"}                                           ;; provided by AWS
                 :cognitect.anomalies/category :cognitect.anomalies/not-found
                 :cognitect.aws.http/status 404
                 :cognitect.aws.error/code "NoSuchKey"})

  ((contains? error-ex                :cognitect.anomalies/category))
  (def alarm-top-resp (aws/invoke cloudwatch {:op :DescribeAlarms}))
  (def bbgum-resp (->> alarm-top-resp :MetricAlarms (map #(select-keys % [:AlarmName :StateValue])) gum-filter))

  (edn/read-string (first bbgum-resp))
  (gum-filter [1 2 3])

  :rcf)
