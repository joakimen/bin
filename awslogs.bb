#!/usr/bin/env bb
(require '[cheshire.core :as json]
         '[babashka.process :as p]
         '[clojure.string :as str])

(defn run [cmd]
  (let [result (p/sh cmd)
        err (:err result)
        exit (:exit result)]
    (when (not (zero? exit))
      (throw (ex-info err {:babashka/exit exit})))
    (:out result)))

(defn fzf [s]
  (let [proc (p/process ["fzf" "-m"]
                        {:in s :err :inherit
                         :out :string})]
    (:out @proc)))

(defn parse-json [data]
  (json/parse-string data true))

(defn list-log-groups []
  (->> (run "aws logs describe-log-groups --output json")
       parse-json
       :logGroups
       (map :logGroupName)))

(defn select-log-group []
  (->> (list-log-groups) (str/join "\n") fzf str/trim))

(defn get-last-log-stream [group-name]
  (->> (run (format "aws logs describe-log-streams --log-group-name %s --max-items 1 --order-by LastEventTime --descending --output json" group-name))
       parse-json :logStreams first :logStreamName))

(defn get-log-events [group stream]
  (run (format "aws logs get-log-events --log-group-name %s --log-stream-name %s --output text" group stream)))

(let [group (select-log-group)
      stream (get-last-log-stream group)]
  (println "group:" group)
  (println "stream:" stream)
  (->> (get-log-events group stream) str/trim println))

(comment
  (def group (first (list-log-groups)))
  (def stream (get-last-log-stream group))
  (get-log-events group stream)

  (get-last-log-stream "/ecs/my-demo-fargate-taskdef")
  ;; this can return an empty list. handle that!
    ;; {
    ;;     "logStreams": []
    ;; }
  group
  stream
  (run (format "aws logs get-log-events --log-group-name %s --log-stream-name %s --output text" group stream))
  (->> (get-log-events group stream))

;; (->> "2022/12/29/[$LATEST]f92cac0f63ff4bcfaa35641974cbb6ac"
  (get-last-log-stream group)

  (->> group get-last-log-stream)
;;   
  )
