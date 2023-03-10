(ns aws.aws-vault.aws-login
  "select N aws profiles, then open them in aws_chrome"
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn select-aws-profiles []
  (let [res (p/sh {:err :inherit} "select-aws-profiles")]
    (when (> (:exit res) 0)
      (System/exit 1)) ;; probs just caught ctrl-c
    (->> res :out str/trim str/split-lines)))

(defn open-in-awschrome [profile]
  (p/shell "aws-chrome" profile))

(pmap open-in-awschrome (select-aws-profiles))
