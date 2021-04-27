(ns kepler16.http.http-kit
  (:require [org.httpkit.server :as http-kit]))

(def defaults {:join? false})

(comment
  {:http/server {:start `(org.httpkit.server/run-jetty
                          handler
                          (merge kepler16.http.http-kit/defaults
                                 {}))
                 :stop `(.close this)}})
