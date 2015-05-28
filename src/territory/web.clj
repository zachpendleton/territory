(ns territory.web
  (:require [org.httpkit.server :refer [run-server]]
            [compojure.route :refer [not-found]]
            [compojure.core :refer [routes GET]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.util.response :refer [response]]
            [territory.middleware.logging :refer [wrap-logger]]
            [cheshire.core :refer [generate-string]]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :refer [debug info]]))

(defn health-check
  []
  (generate-string {:status "OK"}))

(defn app-routes
  []
  (routes (GET "/health-check" [] (health-check))
          (not-found (generate-string {:status "Not Found"}))))

(defrecord WebServer [port handle]
  component/Lifecycle
  (start [this]
    (if (:handle this)
      this
      (let [app (-> (wrap-defaults (app-routes) api-defaults)
                    wrap-logger)]
        (debug ";; starting web server on" port)
        (assoc this :handle (run-server app {:port (:port this)})))))
  (stop [this]
    (if-let [handle (:handle this)]
      (do (debug ";; stopping web server on" port) (handle) (assoc this :handle nil))
      this)))

(defn new-webserver
  [port]
  (map->WebServer {:port port}))
