(ns git.hooks.install-hooks
  "symlinks all .clj files in the current directory (except this script) to ~/.git-hooks.
   
   leverages git config core.hooksPath to determine where to symlink the hooks"
  (:require [babashka.fs :as fs]
            [babashka.process :as p]
            [clojure.string :as str]))

(defn run [cmd]
  (->> cmd (p/sh {:out :string}) :out str/trim))

(def hooks-source-dir (fs/path (fs/home) "bin" "git" "hooks"))
(def hooks-target-dir
  (let [path (run "git config --global --get core.hooksPath")]
    (when (str/blank? path)
      (throw (ex-info "core.hooksPath not set in git config" {:babashka/exit 1})))
    (fs/path (fs/expand-home path))))

(defn link-hook [tgt-file src-file]
  (fs/delete-if-exists tgt-file)
  (fs/create-sym-link tgt-file src-file))

(defn format-filename [file-abspath]
  (let [file-name  (fs/file-name file-abspath)]
    (-> file-name
        (str/replace "_" "-")
        (str/replace (str "." (fs/extension file-name)) ""))))

(->> (fs/glob hooks-source-dir "**.clj")
     (filter #(not (= (fs/path hooks-source-dir "install_hooks.clj") %)))
     (mapv #(link-hook (fs/path hooks-target-dir (format-filename %)) %)))
