(ns aws.aws-vault.aws-chrome
  "open an isolated google chrome window with the specified aws-profile"
  (:require [babashka.process :as p]
            [clojure.string :as str]
            [babashka.fs :as fs]))

(def google-chrome-path  "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")

(defn run [cmd]
  (let [res (p/sh cmd)]
    (when (> (:exit res) 0)
      (some->> res :err str/trim println)
      (System/exit (:exit res)))
    (some->> res :out str/trim)))

(defn create-login-link [profile]
  (run (format "aws-vault login %s --stdout --prompt osascript" profile)))

(let [[profile] *command-line-args*
      aws-login-url (create-login-link profile)
      data-dir (str (fs/path (fs/home) ".aws" "awschrome" profile))
      cache-dir (str (fs/create-temp-dir {:path "/tmp"
                                          :prefix "awschrome_cache_"
                                          :posix-file-permissions "rwx------"}))]

  (p/process google-chrome-path
             "--no-first-run"
             "--start-maximized"
             (str "--user-data-dir=" data-dir)
             (str "--disk-cache-dir=" cache-dir)
             aws-login-url))
