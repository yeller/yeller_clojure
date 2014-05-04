(ns yeller.ring-example-app
  (:require [ring.adapter.jetty9 :as jetty]
            [yeller-clojure-client.ring :as yeller]))

(defn -main [& args]
  (jetty/run-jetty
    (yeller/wrap-ring
      (fn [& args] (assert false "error"))
      {:token "YOUR API KEY HERE"})
    {:port 4001}))
