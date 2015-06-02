(ns territory.database
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :refer [debug]]))

(defprotocol IDatabase
  (fetch [this k])
  (save [this k v ]))

(defrecord Database [store]
  component/Lifecycle
  IDatabase
  (start [this]
    (if (:store this)
      this
      (do
        (debug ";; creating database")
        (assoc this :store (atom {})))))
  (stop [this]
    (if-let [store (:store this)]
      (do
        (debug ";; tearing down database")
        (assoc this :store nil))
      this))
  (fetch [this k]
    (get (:store this) k))
  (save [this k v]
    (swap! (:store this) assoc k v)))

(defn new-database
  []
  (map->Database {}))
