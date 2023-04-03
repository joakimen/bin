(ns aws.aws-vault.aws-login
  "select N aws profiles, then open them in aws_chrome"
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn select-aws-profiles []
  (let [{:keys [exit out]} (p/sh {:err :inherit} "select-aws-profiles")]
    (when-not (zero? exit)
      (System/exit exit))
    (-> out str/trim str/split-lines)))

(defn open-in-awschrome [profile]
  (p/shell "aws-chrome" profile))

(->> (select-aws-profiles)
     (map open-in-awschrome)
     doall)
