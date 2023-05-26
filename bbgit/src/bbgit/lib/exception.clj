(ns bbgit.lib.exception)

(defn die [msg]
  (throw (ex-info msg {:babashka/exit 1})))
