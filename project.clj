(defproject org.cyverse/otel "0.2.6-SNAPSHOT"
  :description "A Clojure library wrapping the opentelemetry client for Java"
  :url "https://github.com/cyverse-de/otel"
  :license {:name "BSD Standard License"
            :url "http://www.iplantcollaborative.org/sites/default/files/iPLANT-LICENSE.txt"}
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  :plugins [[jonase/eastwood "1.4.3"]
            [lein-ancient "0.7.0"]
            [test2junit "1.4.4"]]
  :dependencies [[org.clojure/clojure "1.11.3"]
                 [org.clojure/tools.logging "1.3.0"]
                 [io.opentelemetry/opentelemetry-api "1.40.0"]])
