(ns yeller-clojure-client
  (:require [clojure.walk :refer [stringify-keys]])
  (:import (com.yellerapp.client
             YellerHTTPClient
             YellerClient
             YellerExtraDetail
             YellerErrorHandler)))

(defn default-io-error-handler [backend error]
  (.println *err* (str "Yeller: an io error ocurred whilst talking to yeller: " error)))

(defn default-auth-error-handler [backend error]
  (.println *err* (str "Yeller: an authentication error ocurred whilst talking to yeller: " error)))

(defn ^YellerClient client
  "creates a new client from a map of configuration settings Required settings:
  {:token \"YOUR API KEY HERE\"}

  other settings:
  :auth-error-handler (fn [backend error] (println \"Yeller: an authentication error occurred whilst talking to yeller:\" backend error))
  a function for reporting authentication errors when communicating with
  yeller's servers. You might want this to print out to a log file (by default
  it prints to stderr). Takes a String for the hostname of the api server it
  was talking to during the error, and a Throwable for whatever exception
  ocurred.
  :io-error-handler (fn [backend error] (println \"Yeller: an io error occurred whilst talking to yeller:\" backend error))
  a function for reporting io errors when communicating with
  yeller's servers. You might want this to print out to a log file (by default
  it prints to stderr). Takes a String for the hostname of the api server it
  was talking to during the error, and a Throwable for whatever exception
  ocurred.
  "
  [options]
  (assert (string? (:token options)) "Yeller client must be passed your api key as a string under :token")
  (let [client (YellerHTTPClient. (:token options))]
    (if (or (:io-error-handler options)
            (:auth-error-handler options))
      (.setErrorHandler
        client
        (reify
          YellerErrorHandler
          (reportAuthError [this backend e]
            ((:auth-error-handler options default-auth-error-handler)
               backend e))
          (reportIOError [this backend e]
            ((:auth-io-handler options default-auth-error-handler)
               backend e)))))
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

(defn ^YellerExtraDetail format-extra-detail [extra]
  (-> (YellerExtraDetail.)
    (add-url extra)
    (add-environment extra)
    (add-location extra)))

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
  ([client exception extra]
   (if (or (:environment extra)
           (:location extra)
           (:url extra))
     (let [detail (format-extra-detail extra)]
       (.report client
                exception
                detail
                (stringify-keys (:custom-data extra {}))))
     (.report client
              exception
              (stringify-keys (dissoc extra
                                      :environment
                                      :location
                                      :url))))))
