(defproject org.cyverse/otel "0.2.1"
  :description "A Clojure library wrapping the opentelemetry client for Java"
  :url "https://github.com/cyverse-de/otel"
  :license {:name "BSD Standard License"
            :url "http://www.iplantcollaborative.org/sites/default/files/iPLANT-LICENSE.txt"}
  :deploy-repositories [["releases" :clojars]
                        ["snapshots" :clojars]]
  :plugins [[test2junit "1.2.2"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [io.opentelemetry/opentelemetry-api "0.7.0"]])
