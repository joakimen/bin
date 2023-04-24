(ns jira.issue.get-issue

  "
  # structural problems (issues, parents, children, state), e.g.
  - epics with only closed tasks
  - tasks with only closed subtasks

  NB: issues reference their parents, but not their children.

  ## data structure
  - {:key :parent-key :status}

  ## data to collect:
    - key (JIRA-123)
    - fields.parent.key (JIRA-120)
    - fields.status.name ('Done')

  - filter
    - by project

  # data problems
  - no/short description (needs spec)
  - long time in backlog (trash it?)
  - too many things in 'In Progress' (WIP recommendations)
  - too many things in 'Todo' (backlog being disregarded)
  - unset fields (lack of routine, not being used)

  # manipulation
  - update internal sorting id to reflect column order
  "

  (:require [babashka.curl :as curl]
            [cheshire.core :as json]
            [clojure.string :as str]
            [taoensso.timbre :as log]))

(defn require-env [name]
  (or (System/getenv name)
      (throw (ex-info (str "required env var is unset: " name) {:babashka/exit 1}))))

(def auth {:basic-auth [(require-env "JIRA_API_USER")
                        (require-env "JIRA_API_TOKEN")]})

(def jira-api-url (str "https://" (require-env "JIRA_HOST") "/rest/api/3"))

(defn parse-json [s]
  (json/parse-string s true))

(defn query-api
  ([path] (query-api path {}))
  ([path opts]
   (log/debug "query opts" opts)
   (let [url (str jira-api-url "/" path)
         resp (curl/get url (merge auth opts))]
     (->> resp :body parse-json))))

(defn get-issue [issue-id]
  (query-api (str "issue/" issue-id)))

(defn search-issues [params]
  (query-api "search" {:query-params params}))

(defn stringify-map [m]
  (str/join "," (map (fn [[k v]] (str (name k) "=" v)) (seq m))))

(comment

  (search-issues {:jql (stringify-map {:project "EUR"})
                  :fields (str/join "," ["key" "parent" "status"])})
  ;;
  )
