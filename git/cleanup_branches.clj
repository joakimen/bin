(ns git.cleanup-branches
  (:require [babashka.process :as p]
            [clojure.string :as str]))
(def trunk-branches ["master" "main"])
(def base-branches (conj trunk-branches "develop"))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (if (zero? exit)
      (str/trim out)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))))

(defn get-current-branch [repo]
  (run "git" "-C" repo "branch" "--show-current"))

(defn get-merged-branches [repo]
  (->> (run "git" "-C" repo "branch" "--merged")
       str/split-lines
       (mapv #(str/replace % #"\* " ""))))

(defn confirm []
  (print "confirm [yn]: ")
  (flush)
  (try ;; handle empty user input
    (let [choice (subs (read-line) 0 1)]
      (= (str/lower-case choice) "y"))
    (catch Exception _ false)))

(defn -main [& args]
  (let [repo (or (first args) ".")
        cur-branch (get-current-branch repo)
        merged-branches (get-merged-branches repo)
        to-delete (filterv (fn [branch] (not (some #(= branch %) base-branches))) merged-branches)]

    (when-not (some #(= cur-branch %)  trunk-branches)
      (throw (ex-info (format "current branch [%s] is not a trunk branch, refusing to clean" cur-branch) {:causes "hehe" :babashka/exit 1})))

    (when (empty? to-delete)
      (throw (ex-info "no merged non-base branches to delete" {:babashka/exit 1})))

    (println "will delete:")
    (mapv #(println "-" %) (conj to-delete "demo" "test"))
    (println)
    (when-not (confirm)
      (throw (ex-info "cancelled" {:babashka/exit 1})))

    (println)
    (println "deleting branches..")
    (doseq [branch to-delete]
      (run "git" "-C" repo "branch" "-d" branch))
    (println "done.")))

(apply -main ["."])
