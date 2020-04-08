(ns otel.middleware
  (:require [otel.otel :as otel]
            [otel.http :as http]
            [clojure.string :as string]
            [clojure.tools.logging :as log])
  (:import [io.grpc Context]
           [io.opentelemetry OpenTelemetry]
           [io.opentelemetry.context ContextUtils]))

(defn otel-middleware
  "Middleware that wraps an incoming compojure request in a span describing the
  request"
  [handler]
  (fn [request]
    (if-let [compojure-route (:compojure/route request)]
      (let [span-name (string/join " "
                                   [(string/upper-case (name (compojure-route 0)))
                                    (compojure-route 1)])
            ctx (.extract (.getHttpTextFormat (OpenTelemetry/getPropagators)) (Context/current) request (http/ring-getter))
            extracted-scope (ContextUtils/withScopedContext ctx)]
        (otel/with-span [span [span-name :server]]
          (handler request)))
      (do
        (log/warn "No compojure route information found, not adding opentelemetry span")
        (handler request)))))
