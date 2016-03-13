(ns yeller.clojure.ring
  (:require [yeller.clojure.client :as yeller]
            [ring.util.request :as req]
            [clojure.string :as s]))

(defn request-info [request]
  {:request-method (s/upper-case (name (:request-method request)))
   :user-agent (-> request :headers (get "user-agent"))
   :referrer (-> request :headers (get "referer"))})

(defn format-extra [options request]
  {:url (req/request-url request)
   :custom-data
   {:params (merge (:query-params request) (:form-params request))
    :session (:session request)
    :context (:yeller/context request)
    :http-request (request-info request)}
   :environment (:environment options "production")})

(defn wrap-ring
  [handler options]
  "wraps a ring handler in middleware that sends exceptions to yeller.
   takes a map of options.
   Required:
   {:token \"your api token here\"}
   Optional:
   {:environment \"production\"} ; the name of the environment this ring app is running in
   {:application-packages [\"com.yourapp\"]} : the root package name of your application. Used to mark stacktrace frames as in-app or not (if their package starts with the root, then they'll be marked as in-app)

   if you attach a :yeller/context key in your request map (that contains a
   further map) in your ring request, then this middleware will send that off to
   yeller upon exceptions as well (useful for e.g. user ids, current datomic T
   etc).
   Note, you should put this middleware as far on the inside of the middleware
   stack as possible, so it gets full access to params, session data, etc.
   On the other hand, it should probably sit outside of any middleware you have
   that does e.g. authentication, so it can track errors that happen in that
   middleware"
  (let [client (yeller/client options)]
    (fn [request]
      (try
        (handler request)
        (catch Throwable t
          (yeller/report
            client
            t
            (format-extra options request))
          (throw t))))))
