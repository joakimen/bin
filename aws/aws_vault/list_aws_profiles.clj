(ns aws.aws-vault.list-aws-profiles
  (:require [babashka.process :as p]))

(p/shell "aws" "configure" "list-profiles")
