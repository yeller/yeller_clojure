(defproject yeller-clojure-client "0.2.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [yeller-java-api "0.0.2-SNAPSHOT"]]
  :profiles {:dev {:dependencies [[info.sunng/ring-jetty9-adapter "0.3.0"]]
                   :source-paths ["examples"]}})
