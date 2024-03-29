#!/usr/bin/env bb
(ns git.clone
  "clones github repos, with some helper utils
- search repos by name
- search repos by user
- fuzzy filtering using fzf
- supports cloning multiple repos in parallel
   
install: bbin install https://raw.githubusercontent.com/joakimen/bin/7485cecbac86bbecdbe9d58b486d4da538d30c99/git/clone.clj

usage: 
   $ clone
   $ clone -u joakimen
   $ clone -n babashka
"
  (:require [babashka.process :refer [sh process]]
            [clojure.string :as str]
            [cheshire.core :as json]
            [babashka.cli :as cli]))

(def repo-root
  "repos are cloned here"
  (str (System/getenv "HOME") "/dev/github.com"))

(defn github-username
  "if neither user or repo-args are supplied, read username from git config"
  []
  (-> (sh "git config --get --global github.user") :out str/trim))

(defn fzf
  "filter available repos through fzf multi
  returns newline-separated string with results
  list  see [[github-username]]"
  [s]
  (let [{:keys [out exit]} @(process ["fzf" "-m"]
                                     {:in s :err :inherit
                                      :out :string})]
    (when-not (zero? exit)
      (System/exit exit))
    (str/trim out)))

(defn run
  "run shell command.
   on success, return stdout
   on error, print stderr and exit with exit-code from shell"
  [cmd]
  (let [{:keys [out err exit]} (sh cmd)]
    (when-not (zero? exit)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (str/trim out)))

(defmulti search (fn [repo] (:type repo)))

(defmethod search "user" [repo]
  (let [response (run (format "gh repo list %s --json owner,name --limit 1000" (:value repo)))]
    (->> (json/parse-string response true)
         (map #(assoc {} :user (-> % :owner :login) :name (:name %))))))

(defmethod search "name" [repo]
  (let [response (run (format "gh search repos %s --json owner,name --limit 100 --match name" (:value repo)))]
    (->> (json/parse-string response true)
         (map #(assoc {} :user (-> % :owner :login) :name (:name %))))))

(defn parse-opts [opts]
  (let [rules {:alias {:u :user
                       :n :name}}]
    (cli/parse-opts opts rules)))

(defn map->string
  "converts a map of user/repo pairs to a newline-delimited string for fzf"
  [repo-list]
  (->> repo-list
       (map #(str (:user %) "/" (:name %)))
       (str/join "\n")))

(defn select-one [repo-list]
  (->> repo-list map->string fzf str/split-lines))

;; ugly but works
(defmulti choose-repos
  "expects a map with none, either or both of user/name keys
   returns a repo in the form 'user/name'"
  (fn [{:keys [user name]}] {:user (not (str/blank? user))
                             :name (not (str/blank? name))}))

(defmethod choose-repos {:user false :name false} [_]
  (select-one (search {:type "user" :value (github-username)})))

(defmethod choose-repos {:user true :name false} [{:keys [user]}]
  (select-one (search {:type "user" :value user})))

(defmethod choose-repos {:user false :name true} [{:keys [name]}]
  (select-one (search {:type "name" :value name})))

(defmethod choose-repos {:user true :name true} [{:keys [user name]}]
  [(str user "/" name)])

(defmethod choose-repos :default [m]
  (throw (IllegalArgumentException.
          (str "unsupported args: " m))))

(defn clone
  "clones a single github repo into the dev basedir.
   returns a map containing repo-name, exit-code and stdout/stderr
   for the clone operation"
  [repo]
  (let [repo-dir (str repo-root "/" repo)
        {:keys [exit out err]} (sh (format "gh repo clone %s %s" repo repo-dir))
        msg (str/trim (if (zero? exit)
                        out
                        err))]
    {:repo repo
     :exit exit
     :msg msg}))

(let [opts (parse-opts *command-line-args*)
      repos (choose-repos opts)]
  (println (str "cloning to: " repo-root "\n"
                (->> repos (map #(str "- " %)) (str/join "\n"))))
  (->> repos
       (pmap clone)
       (filter #(> (:exit %) 0))
       (mapv #(println (format "x %s: %s" (:repo %) (:msg %)))))
  (println "done."))
