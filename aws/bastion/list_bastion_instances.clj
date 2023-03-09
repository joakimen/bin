#!/usr/bin/env bb
(ns aws.bastion.list-bastion-instances
  "list bastion hosts, using edn-output meant to be parsed"
  (:require [babashka.process :as p]
            [cheshire.core :as json]))

(defn list-ec2-bastion-hosts []
  (let [res (p/sh "aws" "ec2" "describe-instances" "--filters"
                  "Name=instance-state-name,Values=running"
                  "Name=tag:Name,Values=Bastion")]
    (json/parse-string (:out res) true)))

(defn describe-instance [i]
  (let [tags (into {} ;; KVKV -> KV
                   (map (juxt (comp keyword :Key) :Value))
                   (:Tags i))]
    {:instanceid (:InstanceId i)
     :project (:Project tags)
     :name (:Name tags)}))

(->> (list-ec2-bastion-hosts)
     :Reservations
     (map :Instances)
     (flatten)
     (map describe-instance)
     (println))
