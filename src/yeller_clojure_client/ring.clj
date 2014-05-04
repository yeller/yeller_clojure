(ns yeller-clojure-client.ring
  (:require [yeller-clojure-client :as yeller]
            [ring.util.request :as req]))

(defn format-extra [options request]
  {:url (req/request-url request)
   :custom-data
   {:params (merge (:query-params request) (:form-params request))
    :session (:session request)
    :context (:yeller/context request)}
   :environment (:environment options "development")})

(defn wrap-ring
  [handler options]
  "wraps a ring handler in middleware that sends exceptions to yeller.
   takes a map of options.
   Required:
   {:token \"your api token here\"}
   Optional:
   {:environment \"development\"} ; the name of the environment this ring app is running in

   if you attach a :yeller/context key in your request map (that contains a further map) in your ring request,
   then this middleware will send that off to yeller upon exceptions as well (useful for e.g. user ids, current
   datomic T etc)."
  (let [client (yeller/client options)]
    (fn [request]
      (try
        (handler request)
        (catch Throwable t
          (yeller/report
            client
            t
            (format-extra options request)))))))
