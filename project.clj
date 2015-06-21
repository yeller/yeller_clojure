(defproject yeller-clojure-client "1.2.1"
  :description "Clojure client for yellerapp.com"
  :url "https://github.com/tcrayford/yeller_clojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [yeller-java-api "1.2.0"]]
  :profiles {:dev {:dependencies [[info.sunng/ring-jetty9-adapter "0.3.0"]]
                   :source-paths ["examples"]}})
