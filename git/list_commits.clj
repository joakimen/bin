(ns git.list-commits
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(let [{:keys [out err exit]}
      (p/sh  ["git" "log" "--abbrev-commit" "--pretty=format:%h | %s (%cr) <%an>"] {:out :string})]
  (when-not (zero? exit)
    (throw (ex-info (str/trim err) {:babashka/exit exit})))
  (-> out str/trim println))
