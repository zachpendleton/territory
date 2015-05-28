(ns territory.middleware.logging
  (:require [taoensso.timbre :refer [info]]))

(defn wrap-logger
  [app]
  (fn [req]
    (let [res (app req)
          {:keys [status body]} res
          {:keys [content-type uri]} req]
      (info status uri content-type)
      res)))
