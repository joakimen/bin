(ns bbgit.lib.fzf
  (:require [babashka.process :as p]
            [clojure.string :as str]))

(defn fzf [v]
  (let [{:keys [out exit]} @(p/process ["fzf"]
                                       {:in (str/join "\n" v) :err :inherit
                                        :out :string})]
    (if (zero? exit)
      (str/trim out)
      (System/exit exit))))

(defn fzfm [v]
  (let [{:keys [out exit]} @(p/process ["fzf" "-m"]
                                       {:in (str/join "\n" v) :err :inherit
                                        :out :string})]
    (if (zero? exit)
      (-> out str/trim str/split-lines)
      (System/exit exit))))
