#!/usr/bin/env bb
 (ns missing-in-brewfiles
   (:require [babashka.fs :as fs]
             [babashka.process :as p]
             [clojure.set :as set]
             [clojure.string :as str]))

(def target-brewfile (fs/path (fs/xdg-config-home) "ansible" "brews"))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (if (zero? exit)
      (str/trim out)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))))

(defn list-actual-formulas []
  (when-let [formulae-stdout (run ["brew" "list" "-1" "--formula" "--installed-on-request"])]
    (when-let [formulae (str/split-lines formulae-stdout)]
      formulae)))

(defn list-target-formulas []
  (fs/read-all-lines target-brewfile))

(defn missing-in-right [left right]
  (vec (set/difference (set left) (set right))))

(defn -main [& _]
  ;; print name of target file to stderr to prevent it being copied when piping to pbcopy
  (binding [*out* *err*]
    (println "Installed brews missing in brewfile:" (str target-brewfile)))

  (let [actual-formulas (list-actual-formulas)
        target-formulas (list-target-formulas)
        missing-formulas (missing-in-right actual-formulas target-formulas)]
    (doseq [formula (sort missing-formulas)]
      (println formula))))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))

(comment


  (-main)


  ;;
  )
