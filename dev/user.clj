(ns user
  (:require [clojure.repl :refer :all]
            [clojure.java.javadoc :refer [javadoc]]
            [com.stuartsierra.component :as component]
            [territory.web :refer [new-webserver]]
            [territory.core :as app]
            [clojure.tools.namespace.repl :refer [refresh refresh-all]]))

(def system nil)

(defn init
  []
  (alter-var-root #'system (constantly (app/system {:port 8080}))))

(defn start
  []
  (alter-var-root #'system component/start))

(defn stop
  []
  (alter-var-root #'system component/stop))

(defn go
  []
  (init)
  (start))

(defn reset
  []
  (stop)
  (refresh :after 'user/go))
