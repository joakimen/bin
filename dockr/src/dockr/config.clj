(ns dockr.config)

(def base-config {:engine :docker
                  :version "v1.41"
                  :conn {:uri "unix:///var/run/docker.sock"}})
