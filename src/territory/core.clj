(ns territory.core
  (:require [com.stuartsierra.component :as component]
            [territory.web :refer [new-webserver]]
            [territory.database :refer [new-database]]
            [territory.scheduler :refer [new-scheduler]])
  (:gen-class))

(defn system
  [opts]
  (component/system-map
    :database (new-database)
    :scheduler (new-scheduler)
    :web (component/using
           (new-webserver (:port opts))
           [:database :scheduler])))

(defn -main
  [& args]
  (let [app (system {:port 8080})]
    (component/start app)))
