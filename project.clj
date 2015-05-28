(defproject territory "0.0.1"
  :description "Territory game server"
  :url "https://github.com/zachpendleton/territory"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 ;; web
                 [http-kit "2.1.18"]
                 [compojure "1.1.6"]
                 [ring "1.4.0-RC1"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-json "0.3.1"]
                 [cheshire "5.5.0"]

                 ;; architecture
                 [com.stuartsierra/component "0.2.3"]

                 ;; utils
                 [com.taoensso/timbre "4.0.0-beta1"]
                 [environ "1.0.0"]
                 [org.clojure/tools.cli "0.3.1"]]
  :main ^:skip-aot territory.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev [:dev-common :dev-overrides]
             :dev-common {:dependencies [[org.clojure/tools.namespace "0.2.7"]]
                          :source-paths ["dev"]
                          :repl-options {:init-ns user}}
             :dev-overrides {}})
