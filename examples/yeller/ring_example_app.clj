(ns yeller.ring-example-app
  (:require [ring.adapter.jetty9 :as jetty]
            [ring.middleware.params :as params]
            [ring.middleware.session :as session]
            [yeller.clojure.ring :as yeller-ring]))

(defn -main [& args]
  (jetty/run-jetty
    (params/wrap-params
      (session/wrap-session
        (yeller-ring/wrap-ring
          (fn [& args] (assert false "error"))
          {:token "yk_w_60f4e12d6ead7b75b4b8206f1f8619adbcd69ffe4a12b9b06cb0f569b2f08b0c"
           :application-packages ["yeller.ring_example_app"]})))
    {:port 4001}))
