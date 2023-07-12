(ns bgh.cli
  (:require [babashka.fs :refer [which]]
            [babashka.process :refer [shell]]))

(when-not (which "gh")
  (throw (ex-info "missing required binary gh" {:babashka/exit 1})))

(defn- gh [& args]
  (apply println "gh" args)
  (apply shell "gh" args))

(defn allow-workflow-writes [_]
  (gh "api" "-X" "PUT" "repos/{owner}/{repo}/actions/permissions/workflow"
      "-f" "default_workflow_permissions=write"))

(defn enable-branch-update-button [_]
  (gh "repo" "edit" "--allow-update-branch"))

(defn auto-delete-head-branches [_]
  (gh "api" "-X" "PATCH" "repos/{owner}/{repo}"
      "-F" "delete_branch_on_merge=true" "--silent"))
