(ns macos.disable-keychain-timeout
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.string :as str]))

;; disable automatic lock on one or more keychains

(defn fzf [s]
  (let [{:keys [exit out]} @(p/process ["fzf" "-m"]
                                       {:in s :err :inherit
                                        :out :string})]
    (when-not (zero? exit)
      (System/exit exit))
    (str/trim out)))

(defn fzfv [v]
  (->> (str/join "\n" v)
       (fzf)
       (str/split-lines)))

(let [keychain-dir (fs/path (fs/home) "Library" "Keychains")
      all-keychains (->> (fs/list-dir keychain-dir)
                         (filter #(str/ends-with? % "keychain-db")))
      chosen-keychains (fzfv all-keychains)]
  (->> (chosen-keychains)
       (pmap #(p/shell "security" "set-keychain-settings" %))
       (doall)))
