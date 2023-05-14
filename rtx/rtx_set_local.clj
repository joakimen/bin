(ns rtx.rtx-set-local
  (:require [babashka.process :as p]
            [clojure.string :as str]
            [cheshire.core :as json]))

(defn fzf [s]
  (let [{:keys [out exit]} @(p/process ["fzf" "-m"]
                                       {:in s :err :inherit
                                        :out :string})]
    (when-not (zero? exit)
      (System/exit exit))
    (str/trim out)))

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (if (zero? exit)
      (str/trim out)
      (throw (ex-info (str/trim err) {:babashka/exit exit})))))

(defn -main [& _]
  (let [data (json/parse-string (run "rtx ls --json") true)
        plugins (mapv name (keys data))
        plugin (fzf (str/join "\n" plugins))
        version (->> plugin keyword (get data) (map :version) (str/join "\n") fzf)]

    (when (or (str/blank? plugin) (str/blank? version))
      (throw (ex-info "couldn't parse plugin or version data" {:babashka/exit 1})))

    (run "rtx" "use" (format "%s@%s" plugin version))))

(-main)
