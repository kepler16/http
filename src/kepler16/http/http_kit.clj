(ns kepler16.http.http-kit
  (:require [org.httpkit.server :as http-kit]
            [integrant.core :as ig]))

(defmethod ig/init-key :adapter/http-kit [_ opts]
  (let [handler (atom (delay (:handler opts)))
        options (-> opts (dissoc :handler) (assoc :join? false))]
    {:handler handler
     :server  (http-kit/run-server (fn [req] (@@handler req)) options)}))

(defmethod ig/halt-key! :adapter/http-kit [_ {:keys [server]}]
  (.stop server))

(defmethod ig/suspend-key! :adapter/http-kit [_ {:keys [handler]}]
  (reset! handler (promise)))

(defmethod ig/resolve-key :adapter/http-kit [_ {:keys [server]}]
  server)

(defmethod ig/resume-key :adapter/http-kit [key opts old-opts old-impl]
  (if (= (dissoc opts :handler) (dissoc old-opts :handler))
    (do (deliver @(:handler old-impl) (:handler opts))
        old-impl)
    (do (ig/halt-key! key old-impl)
        (ig/init-key key opts))))
