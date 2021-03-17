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
  (let [span-name (if-let [compojure-route (:compojure/route request)]
                    (string/join " "
                                 [(string/upper-case (name (compojure-route 0)))
                                 (str (:context request "") (compojure-route 1))])
                    (do
                      (log/debug "No compojure route info found. Using base Ring definitions, which may be less useful.")
                      (string/join " "
                                 [(string/upper-case (name (:content-type request :unknown)))
                                  (:uri request "(unknown path)")])))]
    (otel/with-span [span [span-name {:kind :server}]]
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
