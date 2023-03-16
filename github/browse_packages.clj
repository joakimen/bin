(ns github.browse-packages
  "script to browse packages-page of a given repo. cba with auth atm"
  (:require [babashka.fs :refer [home path]]
            [babashka.process :refer [shell]]
            [clojure.java.browse :refer [browse-url]]
            [clojure.string :refer [join replace split-lines trim]]))

(def repo-root (str (path (home) "dev" "github.com")))

(defn remove-trailing-slash [s]
  (replace s #"/$" ""))

(let [repos (->> (shell {:out :string :dir repo-root} "fd -t d --exact-depth 2")
                 :out split-lines (map remove-trailing-slash))
      repo (->> (shell {:err :inherit :out :string :in (join "\n" repos)} "fzf")
                :out trim)
      package-url (str "https://github.com/" repo "/packages/")]
  (browse-url package-url))
