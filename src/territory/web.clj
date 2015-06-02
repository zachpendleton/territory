(ns territory.web
  (:require [org.httpkit.server :refer [run-server]]
            [compojure.route :refer [not-found]]
            [compojure.core :refer [routes GET POST]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.util.response :refer [response]]
            [territory.middleware.logging :refer [wrap-logger]]
            [cheshire.core :refer [generate-string]]
            [com.stuartsierra.component :as component]
            [territory.database :as database]
            [taoensso.timbre :refer [debug info]])
  (:import [java.util UUID]))

(defn create-game
  [db]
  (let [id (str (UUID/randomUUID))
        new-game {:state :created :players []}]
    (database/save db id new-game)
    (generate-string {:game (assoc new-game :id id)})))

(defn health-check
  []
  (generate-string {:status "OK"}))

(defn app-routes
  [database]
  (routes (GET "/health-check" [] (health-check))
          (POST "/games" [] (create-game database))
          (not-found (generate-string {:status "Not Found"}))))

(defrecord WebServer [port handle]
  component/Lifecycle
  (start [this]
    (if (:handle this)
      this
      (let [app (-> (wrap-defaults (app-routes (:database this)) api-defaults)
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
