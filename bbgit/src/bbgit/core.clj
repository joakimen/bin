(ns bbgit.core
  (:require [bbgit.lib.exception :refer [die]]
            [bbgit.lib.git :as g]
            [bbgit.lib.gum :as gum]
            [fzf.core :refer [fzf]]))

(defn switch-branch []
  (let [branches (g/list-branches)]
    (when (zero? (count branches))
      (die "no branches found"))
    (-> (fzf {:in branches}) g/switch-branch)))

(defn revert-commit []
  (let [commits (g/list-commits)
        commit (->> (fzf {:in commits}))]
    (when-not commit
      (throw (ex-info nil {:babashka/exit 1})))
    (let [commit (re-find #"^[a-f0-9]+" commit)]
      (println "Reverting commit" commit)
      (g/revert-commit commit))))

(defn checkout-pull-request []
  (let [pull-requests (g/list-pull-requests)
        pull-request (->> (fzf {:in pull-requests}) (re-find #"\d+"))]
    (when-not pull-request
      (die "couldn't parse selected pull request"))
    (println "Checking out pull request" pull-request)
    (g/checkout-pull-request pull-request)))

(defn checkout-commit []
  (let [commits (g/list-commits)
        commit (->> (fzf {:in commits}) (re-find #"^[a-f0-9]+"))]
    (when-not commit
      (die "couldn't parse selected commit"))
    (println "Checking out commit" commit)
    (g/checkout-commit commit)))

(defn list-branches []
  (prn (g/list-branches)))

(defn delete-branches []
  (let [branches (g/list-branches)
        to-delete (-> (fzf {:multi true
                            :in branches}))]
    (when (empty? branches)
      (die "no branches selected"))
    (println "Will delete branches")
    (doseq [b branches]
      (println "-" b))
    (when (gum/confirm)
      (doseq [b branches]
        (println "Deleting branch" b)
        (g/delete-branch b))
      (println "Done."))))
