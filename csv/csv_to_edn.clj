(ns csv-to-edn "parse csv to edn, map headers to keys"
    (:require [clojure.data.csv :as csv]))

(defn read-csv [file]
  (-> file slurp csv/read-csv))

(defn csv-to-edn [csv]
  (let [headers (first csv)
        body (rest csv)]
    (->> body
         (map (partial zipmap (map keyword headers))))))

(let [file (or (first *command-line-args*)
               (throw (ex-info "no file given" {:babashka/exit 1})))]
  (-> file
      read-csv
      csv-to-edn
      prn))
