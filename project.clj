(defproject yeller-clojure-client "1.0.0"
  :description "Clojure client for yellerapp.com"
  :url "https://github.com/tcrayford/yeller_clojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [yeller-java-api "0.4.1"]]
  :profiles {:dev {:dependencies [[info.sunng/ring-jetty9-adapter "0.3.0"]]
                   :source-paths ["examples"]}})
