(ns aws.aws-vault.aws-exec
  "select 1 aws profile, then open a shell-session with it using aws-vault"
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn select-aws-profile []
  (let [{:keys [exit out]} (p/sh {:err :inherit} "select-aws-profiles")]
    (when-not (zero? exit)
      (System/exit 1)) ;; probs just caught ctrl-c
    (->> out str/trim str/split-lines first)))

(p/shell "aws-vault" "exec" (select-aws-profile))
