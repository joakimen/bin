(ns run-crawler "runs an aws crawler and waits for completion"
    (:require [clojure.string :as str]
              [babashka.process :as p]
              [cheshire.core :as json]
              [taoensso.timbre :as log]))

(defn list-crawlers []
  (log/info "finding crawlers")
  (let [{:keys [out err exit]} (p/sh "aws glue list-crawlers")]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (-> out (json/parse-string true) :CrawlerNames)))

(defn fzf [v]
  (let [{:keys [out exit]} @(p/process ["fzf"]
                                       {:in (str/join "\n" v)
                                        :err :inherit
                                        :out :string})]
    (if (zero? exit)
      (str/trim out)
      (System/exit exit))))

(defn run-crawler [crawler-name]
  (log/info "starting crawler" {:crawler-name crawler-name})
  (:out (p/shell {:out :string} "aws" "glue" "start-crawler" "--name" crawler-name)))

(defn -main [& _]
  (let [crawlers (list-crawlers)]
    (if (empty? crawlers)
      (throw (ex-info "no crawlers found" {:babashka/exit 1}))
      (->> crawlers fzf run-crawler))))

(-main)
