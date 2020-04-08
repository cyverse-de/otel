(ns otel.middleware
  (:require [otel.otel :as otel]
            [otel.http :as http]
            [clojure.string :as string]
            [clojure.tools.logging :as log])
  (:import [java.io Closeable]
           [io.grpc Context]
           [io.opentelemetry OpenTelemetry]
           [io.opentelemetry.context ContextUtils]))

(defn- do-span
  [request handler]
  (if-let [compojure-route (:compojure/route request)]
    (let [span-name (string/join " "
                                 [(string/upper-case (name (compojure-route 0)))
                                  (compojure-route 1)])]
          (otel/with-span [span [span-name :server]]
            (handler request)))
    (do
      (log/warn "No compojure route information found, not adding opentelemetry span")
      (handler request))))

(defn- extract-context
  [request]
  (try
    (ContextUtils/withScopedContext
      (.extract
        (.getHttpTextFormat (OpenTelemetry/getPropagators))
        (Context/current)
        request
        (http/ring-getter)))
    (catch Exception e
      (log/warn "Got an exception trying to extract context from an incoming HTTP request", e)
      (reify Closeable
        (close [this] nil)))))

(defn otel-middleware
  "Middleware that wraps an incoming compojure request in a span describing the
  request"
  [handler]
  (fn [request]
    (with-open [_ (extract-context request)]
      (do-span request handler))))
