#!/usr/bin/env bb
(require '[babashka.pods :as pods])
(pods/load-pod 'org.babashka/fswatcher "0.0.3")

(require '[pod.babashka.fswatcher :as fw]
         '[babashka.fs :as fs]
         '[babashka.process :as p])

(defn exec [cmd]
  (println "executing cmd" cmd)
  (p/shell {:continue true} cmd)
  (println "cmd done"))

;; run @cmd whenever @dir changes
(let [[dir cmd] *command-line-args*]

  (or (fs/directory? dir)
      (println (format "%s: No such file or directory" dir))
      (System/exit 1))

  (exec cmd) ;; run immediately, then on each file-change

  (fw/watch dir (fn [event]
                  (println "event:" event)
                  (exec cmd)) {:recursive true
                               :delay-ms 100})
  @(promise))
