(ns yeller.ring-example-app
  (:require [ring.adapter.jetty9 :as jetty]
            [ring.middleware.params :as params]
            [ring.middleware.session :as session]
            [yeller-clojure-client.ring :as yeller]))

(defn -main [& args]
  (jetty/run-jetty
    (params/wrap-params
      (session/wrap-session
        (yeller/wrap-ring
          (fn [& args] (assert false "error"))
          {:token "YOUR API TOKEN HERE"
           :application-packages ["yeller.ring_example_app"]})))
    {:port 4001}))
