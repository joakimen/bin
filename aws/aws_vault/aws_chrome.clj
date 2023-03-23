(ns aws.aws-vault.aws-chrome
  "open an isolated google chrome window with the specified aws-profile"
  (:require [babashka.process :as p]
            [clojure.string :as str]
            [babashka.fs :as fs]))

(def google-chrome-path  "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")

(defn run [& args]
  (let [{:keys [out err exit]} (apply p/sh args)]
    (when (not (zero? exit))
      (throw (ex-info (str/trim err) {:babashka/exit exit})))
    (str/trim out)))

(defn create-login-link [profile]
  (run "aws-vault" "login" profile "--stdout" "--prompt" "osascript"))

(let [[profile] *command-line-args*
      aws-login-url (create-login-link profile)
      data-dir (str (fs/path (fs/home) ".aws" "awschrome" profile))
      cache-dir (str (fs/create-temp-dir {:path "/tmp"
                                          :prefix "awschrome_cache_"
                                          :posix-file-permissions "rwx------"}))]

  (p/process google-chrome-path
             "--no-first-run"
             "--start-maximized"
             "--new-window"
             (str "--user-data-dir=" data-dir)
             (str "--disk-cache-dir=" cache-dir)
             aws-login-url))
