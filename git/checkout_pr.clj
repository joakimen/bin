(ns git.checkout-pr
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(let [pull-request (-> (p/process "gh pr list")
                       (p/process {:err :inherit :out :string} "fzf")
                       deref :out str/trim)]
  (p/shell {:continue :true} "gh" "pr" "checkout" (re-find #"\d+" pull-request)))
