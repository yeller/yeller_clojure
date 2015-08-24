(ns yeller.clojure.client
  (:require [clojure.walk :refer [stringify-keys]]
            [clojure.edn :as edn]
            [clojure.java.io :as io])
  (:import (com.yellerapp.client
             YellerHTTPClient
             YellerClient
             YellerExtraDetail
             YellerErrorHandler)
           (com.fasterxml.jackson.databind
             JsonSerializer
             ObjectMapper
             module.SimpleModule)
           (com.fasterxml.jackson.core Version JsonGenerator)
           (java.io PrintWriter)))

(def client-version "yeller-clojure-client: 1.3.3")

(defn default-io-error-handler [backend ^Throwable error]
  (.println ^PrintWriter *err* (str "Yeller: an io error ocurred whilst talking to yeller: "))
  (if error
    (.printStackTrace error ^PrintWriter *err*)))

(defn default-auth-error-handler [backend ^Throwable error]
  (.println ^PrintWriter *err* (str "Yeller: an authentication error ocurred whilst talking to yeller: " error))
  (if error
    (.printStackTrace error ^PrintWriter *err*)))

(defn set-error-handlers! [^YellerHTTPClient client options]
  (if (or (:auth-error-handler options)
          (:io-error-handler options))
    (let [auth (:auth-error-handler options default-auth-error-handler)
          io (:io-error-handler options default-io-error-handler)]
      (.setErrorHandler
        client
        (reify
          YellerErrorHandler
          (reportAuthError [this backend e]
            (auth backend e))
          (reportIOError [this backend e]
            (io backend e)))))))

(defn- ^"[Ljava.lang.String;" make-endpoint-array
  "turns a seq of endpoints into
  an array of endpoints"
  [endpoints]
  (let [^"[Ljava.lang.String;" a (make-array String (count endpoints))]
    (loop  [i 0]
      (if (<= (count endpoints) i)
        a
        (do
          (aset a i (nth endpoints i))
          (recur (inc i)))))))

(defn named->string [maybe-named]
  (if (string? maybe-named)
    maybe-named
    (name maybe-named)))

(defn- ^"[Ljava.lang.String;" make-app-package-array
  "turns a seq of application packages into
  an array of application packages"
  [app-packages]
  (let [^"[Ljava.lang.String;" a (make-array String (count app-packages))]
    (loop  [i 0]
      (if (<= (count app-packages) i)
        a
        (do
          (aset a i (named->string (nth app-packages i)))
          (recur (inc i)))))))

(defn set-urls! [^YellerHTTPClient client options]
  (when-let [endpoints (:endpoints options)]
    (assert (every? string? endpoints) (str "was given endpoints that weren't strings under :endpoints, got: " (pr-str endpoints)))
    (.setUrls client (make-endpoint-array (:endpoints options)))))

(defn set-debug! [^YellerHTTPClient client options]
  (if (:debug options)
    (.enableDebug client))
  client)

(defn local-project-clj-package-name []
  (try
    (second (edn/read-string (slurp "project.clj")))
    (catch Exception e
      nil)))

(defn uberjar-project-clj-package-name []
  (try
    (second (edn/read-string (slurp (io/resource "project.clj"))))
    (catch Exception e
      nil)))

(defn default-application-packages []
  (filter
    identity
    [(local-project-clj-package-name)
     (uberjar-project-clj-package-name)]))

(defn set-application-packages! [^YellerHTTPClient client options]
  (if (:application-packages options)
    (.setApplicationPackages client (make-app-package-array (:application-packages options)))
    (.setApplicationPackages client (make-app-package-array (default-application-packages)))))

(def named-serializer
  (proxy [JsonSerializer] []
    (serialize [named generator provider]
      (if (namespace named)
        (.writeString ^JsonGenerator generator (str (namespace named) "/" (name named)))
        (.writeString ^JsonGenerator generator (name named))))))

(def var-serializer
  (proxy [JsonSerializer] []
    (serialize [v generator provider]
      (if-let [n (:ns (meta v))]
        (.writeString ^JsonGenerator generator ^String (str n "/" (:name (meta v))))
        (.writeString ^JsonGenerator generator ^String (:name (meta v)))))))

(defn ^ObjectMapper object-mapper
  "a Jackson ObjectMapper that serializes symbols, keywords and vars 'correctly'"
  []
  (let [mapper (ObjectMapper.)
        module (SimpleModule.)]
   (.addSerializer module clojure.lang.Keyword named-serializer)
   (.addSerializer module clojure.lang.Var var-serializer)
   (.addSerializer module clojure.lang.Symbol named-serializer)
   (.registerModule mapper module)
    mapper))

(defn ^YellerClient client
  "Creates a new client from a map of configuration settings. Required settings:
  {:token \"YOUR API KEY HERE\"}

  other settings:
  * :environment \"production\" The environment the application is deployed into.
  * :application-packages [\"myapp\"] (optional)
    A seq of strings of package names. Used to mark stacktrace frames as coming from the application or not.
    Defaults to a name read from the application's project.clj
  * :auth-error-handler (fn [backend error] (println \"Yeller: an authentication error occurred whilst talking to yeller:\" backend error))
    A function for reporting authentication errors when communicating with
    yeller's servers. You might want this to print out to a log file (by default
    it prints to stderr). Takes a String for the hostname of the api server it
    was talking to during the error, and a Throwable for whatever exception
    ocurred.
  * :io-error-handler (fn [backend error] (println \"Yeller: an io error occurred whilst talking to yeller:\" backend error))
    A function for reporting io errors when communicating with
    yeller's servers. You might want this to print out to a log file (by default
    it prints to stderr). Takes a String for the hostname of the api server it
    was talking to during the error, and a Throwable for whatever exception
    ocurred."
  [options]
  (assert (string? (:token options)) (str "Yeller client must be passed your api key as a string under :token, but got " (pr-str (:token options)) " from (:token options)"))
  (assert (and (sequential? (:application-packages options []))
            (every? string? (:application-packages options []))) (str "Yeller :application-packages must be a sequential collection of strings."))
  (let [client (YellerHTTPClient. (:token options))]
    (.withObjectMapper client (object-mapper))
    (set-error-handlers! client options)
    (set-urls! client options)
    (set-debug! client options)
    (set-application-packages! client options)
    client))

(defn ^YellerExtraDetail add-url [^YellerExtraDetail detail extra]
  (if (string? (:url extra))
    (.withUrl detail (:url extra))
    detail))

(defn ^YellerExtraDetail add-environment [^YellerExtraDetail detail extra]
  (if (string? (:environment extra))
    (.withApplicationEnvironment detail (:environment extra))
    detail))

(defn ^YellerExtraDetail add-location [^YellerExtraDetail detail extra]
  (if (string? (:location extra))
    (.withLocation detail (:location extra))
    detail))

(defn ^YellerExtraDetail add-client-version [^YellerExtraDetail detail extra]
  (if (string? (:client-version extra))
    (.withClientVersion detail (:client-version extra))
    (.withClientVersion detail client-version)))

(defn ^YellerExtraDetail format-extra-detail [extra]
  (-> (YellerExtraDetail.)
    (add-url extra)
    (add-environment extra)
    (add-location extra)
    (add-client-version extra)))

(defn add-ex-data [exception current-custom-data]
  (if-let [data (ex-data exception)]
    (merge {:ex-data data} current-custom-data)
    current-custom-data))

(defn report
  "reports an exception to yeller's servers.
   Optionally takes a map of extra detail. Currently supported extra detail:
   :url \"http://example.com\"
   :environment \"production\"
   :location \"my-ring-handler\"
   :custom-data
   {:params ...}
   "
  ([client exception] (report client exception {}))
  ([^YellerHTTPClient client ^Throwable exception extra]
   (if (or (:environment extra)
           (:location extra)
           (:url extra))
     (let [detail (format-extra-detail extra)]
       (.report client
                exception
                detail
                (stringify-keys (add-ex-data exception (:custom-data extra {})))))
     (.report client
              exception
              ^java.util.Map (stringify-keys (add-ex-data
                                exception
                                (:custom-data extra {})))))))
