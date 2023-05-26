(ns bbgit.lib.git
  (:require [bbgit.lib.sh :refer [sh]]
            [clojure.string :as str]))

(defn list-branches []
  (str/split-lines (sh "git for-each-ref --format='%(refname:short)' refs/heads")))

(defn list-merged-branches []
  (str/split-lines (sh "git for-each-ref --format='%(refname:short)' refs/heads" "--merged")))

(defn switch-branch [branch]
  (sh "git" "switch" branch))

(defn list-commits []
  (str/split-lines (sh "git" "log" "--abbrev-commit" "--pretty=format:%h | %s (%cr) <%an>")))

(defn revert-commit [commit]
  (sh "git" "revert" commit))

(defn current-branch []
  (sh "git" "branch" "--show-current"))

(defn list-pull-requests []
  (->> (sh "gh" "pr" "list")
       str/split-lines
       (filterv #(not (str/blank? %)))))

(defn checkout-pull-request [pull-request]
  (sh "gh" "pr" "checkout" pull-request))

(defn checkout-commit [commit]
  (sh "git" "checkout" commit))

(defn delete-branch [branch]
  (sh "git" "branch" "-D" branch))
