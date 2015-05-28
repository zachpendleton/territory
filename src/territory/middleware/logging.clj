(ns territory.middleware.logging
  (:require [taoensso.timbre :refer [info]]
            [clojure.string :refer [upper-case]]))

(defn wrap-logger
  [app]
  (fn [req]
    (let [res (app req)
          {:keys [status body]} res
          {:keys [content-type uri request-method]} req
          request-method (-> request-method name upper-case)]
      (info (format "\"%s %s HTTP/1.1\" %d" request-method uri status))
      res)))
