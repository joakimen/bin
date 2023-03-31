#!/usr/bin/env bb
(ns jwt.jwt-decode "decode header and payload from a JWT token, read from stdin"
    (:require [cheshire.core :as json]
              [clojure.string :as str]))

(defn b64-decode [s]
  (slurp (.decode (java.util.Base64/getDecoder) s)))

(let [encoded (slurp *in*)
      fragments (str/split encoded #"\.")
      [header payload] (->> fragments
                            (take 2) ;; header and payload
                            (map b64-decode)
                            (map #(json/parse-string % true)))
      result (json/generate-string {:header header
                                    :payload payload} {:pretty true})]
  (println result))
