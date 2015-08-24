(defproject yeller-clojure-client "1.3.2"
  :description "Clojure client for yellerapp.com"
  :url "https://github.com/tcrayford/yeller_clojure"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [yeller-java-api "1.3.0"]]
  :profiles {:dev {:dependencies [[info.sunng/ring-jetty9-adapter "0.3.0"]]
                   :source-paths ["examples"]}})
