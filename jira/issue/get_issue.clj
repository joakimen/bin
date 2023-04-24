(ns jira.issue.get-issue
  (:require [babashka.curl :as curl]
            [babashka.process :as p]))

(defn require-env [name]
  (or (System/getenv name)
      (throw (ex-info (str "required env var is unset: " name) {:babashka/exit 1}))))

(def auth {:basic-auth [(require-env "JIRA_API_USER")
                        (require-env "JIRA_API_TOKEN")]})

(def jira-api-url (str "https://" (require-env "JIRA_HOST") "/rest/api/3"))

(defn get-issue [issue-id]
  (let [url (str jira-api-url "/issue/" issue-id)
        resp (curl/get url auth)]
    (:body resp)))

(let [issue (get-issue (first *command-line-args*))]
  (p/shell {:in issue} "jq"))
