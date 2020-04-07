(ns otel.otel
  (:import [io.opentelemetry OpenTelemetry]
           [io.opentelemetry.trace Span$Kind]))

(defn tracer
  "Set up a tracer using the OpenTelemetry API"
  []
  (let [tracer-provider (OpenTelemetry/getTracerProvider)]
    (.get tracer-provider "otel.otel clojure")))

(def span-kinds
  {:internal Span$Kind/INTERNAL
   :server   Span$Kind/SERVER
   :client   Span$Kind/CLIENT
   :producer Span$Kind/PRODUCER
   :consumer Span$Kind/CONSUMER})

(defn span
  ([span-name]
   (span span-name :internal))
  ([span-name kind-key]
   (-> (.spanBuilder (tracer) span-name)
       (.setSpanKind (span-kinds kind-key))
       (.startSpan))))

(defn span-scope
  "Open a Scope for a Span, to be used with with-open"
  [span]
  (.withSpan (tracer) span))
