(ns kepler16.http.jetty
  (:require [ring.adapter.jetty :as jetty]))

(def defaults {:join? false})

(comment
  (defn handler [req]
    {:body "hi"})

  {:http/server (let [jetty-options (merge kepler16.http.jetty/defaults {:port 8080})
                      handler #'handler]
                  {:start `(ring.adapter.jetty/run-jetty ~handler ~jetty-options)
                   :stop '(.stop this)})})
