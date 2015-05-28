(ns territory.core
  (:require [com.stuartsierra.component :as component]
            [territory.web :refer [new-webserver]])
  (:gen-class))

(defn system
  [opts]
  (component/system-map :web (new-webserver (:port opts))))

(defn -main
  [& args]
  (let [app (system {:port 8080})]
    (component/start app)))
