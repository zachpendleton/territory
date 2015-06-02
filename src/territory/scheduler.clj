(ns territory.scheduler
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :refer [debug info]])
  (:import [java.util.concurrent Executors TimeUnit]))

(defprotocol IScheduler
  (schedule [this f wait] [this f wait args]))

(defrecord Scheduler [pool]
  component/Lifecycle
  IScheduler
  (start [this]
    (if (nil? pool)
      (assoc this :pool (Executors/newScheduledThreadPool 2))
      this))
  (stop [this]
    (if (nil? pool)
      this
      (assoc this :pool nil)))
  (schedule
    [this f wait]
    (schedule this f wait []))
  (schedule
    [this f wait args]
     (.schedule (:pool this)
                (reify Runnable (run [_] (apply f args)))
                wait
                TimeUnit/MILLISECONDS)))

(defn new-scheduler
  []
  (map->Scheduler {}))
