(ns territory.core
  (:require [com.stuartsierra.component :as component]
            [territory.web :refer [new-webserver]]
            [territory.database :refer [new-database]])
  (:gen-class))

(defn system
  [opts]
  (component/system-map
    :database (new-database)
    :web (component/using
           (new-webserver (:port opts))
           [:database])))

(defn -main
  [& args]
  (let [app (system {:port 8080})]
    (component/start app)))
