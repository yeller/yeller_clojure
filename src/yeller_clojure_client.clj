(ns yeller-clojure-client
  (:require [clojure.walk :refer [stringify-keys]])
  (:import (com.yellerapp.client YellerHTTPClient YellerClient YellerExtraDetail)))

(defn ^YellerClient client
  "creates a new client from a map of configuration settings Required settings:
   {:token \"YOUR API KEY HERE\"}
   other settings:
   :error-handler (fn [error] (println error))
   a function for reporting errors when communicating with yeller's servers
  "
  [options]
  (assert (string? (:token options)) "Yeller client must be passed your api key as a string under :token")
  (let [client (YellerHTTPClient. (:token options))]
    client
    )
  )

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
   (let [detail (format-extra-detail extra)]
     (.report client detail (stringify-keys (dissoc extra
                                                    :environment
                                                    :location
                                                    :url))))))
