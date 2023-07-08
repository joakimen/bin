(ns add-aws-dep
  (:require [babashka.http-client :as c]
            [fzf.core :refer [fzf]]
            [zprint.core :refer [zprint-str]]))

(def api-version-url "https://raw.githubusercontent.com/cognitect-labs/aws-api/main/latest-releases.edn")

(defn get-versions
  "fetch latest api versions from cognitect repo"
  []
  (let [{:keys [status body]} (c/get api-version-url)]
    (when-not (= 200 status)
      (throw (ex-info (str "non-200 response from api version url: " api-version-url) {:babashka/exit 1})))
    body))

(defn parse-versions
  "read api versions into edn and keep relevant keys"
  [body]
  (-> body read-string
      (update-vals #(dissoc % :aws/serviceFullName))))

(defn select-deps
  "select one or more deps from the list"
  [deps]
  (->> (seq deps)
       (fzf {:multi true})
       (map read-string)
       flatten
       (apply hash-map)))

(defn add-dep
  "merge selected deps with deps in specified outfile
  - format file using zprint
  - doesn't handle non-existing outfile yet"
  [file m]
  (let [content (-> file slurp read-string)
        updated-content (update content :deps merge m)]
    (spit file (zprint-str updated-content {:map {:sort? false
                                                  :comma? false}}))))

(let [outfile (or (first *command-line-args*) "deps.edn")
      body (get-versions)
      all-versions (parse-versions body)
      selected-versions (select-deps all-versions)]
  (when-not (empty? selected-versions)
    (println "adding" (-> selected-versions keys count) "deps to" outfile)
    (add-dep outfile selected-versions)))


(count (keys {:name "kevin" :age 1}))
