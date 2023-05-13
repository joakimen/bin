#!/usr/bin/env bb
(ns aws.get-api-versions
  "fetch and print latest aws-api versions in a format that's easy to copy into deps.edn
   still needs some tidying of output, but it's a start"
  (:require [clojure.edn :as edn]))

(let [api-versions (slurp "https://raw.githubusercontent.com/cognitect-labs/aws-api/main/latest-releases.edn")
      data (-> api-versions
               edn/read-string
               (update-vals #(select-keys % [:mvn/version]))
               sort)]
  (mapv println data)
  ;;
  )
