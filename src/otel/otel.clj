(ns otel.otel
  (:import [io.opentelemetry OpenTelemetry]
           [io.opentelemetry.trace Span$Kind Tracer TracingContextUtils]))

(defn ^Tracer tracer
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
   (span span-name :internal []))
  ([span-name kind-key]
   (span span-name kind-key []))
  ([span-name kind-key link-spans]
   (let [builder (->
           (.spanBuilder (tracer) span-name)
           (.setSpanKind (span-kinds kind-key))
           (.setAttribute "thread.id" (.getId (Thread/currentThread)))
           (.setAttribute "thread.name" (.getName (Thread/currentThread))))]
     (when (seq link-spans)
       (.setParent builder (TracingContextUtils/getCurrentSpan))
       (doall (map (fn [s] (.addLink builder (.getContext s))) link-spans)))
     (.startSpan builder))))

(defn span-scope
  "Open a Scope for a Span, to be used with with-open"
  [span]
  (.withSpan (tracer) span))

(defmacro with-span
  [span-binding & body]
  (let [[span-sym span-args] span-binding]
    `(let [span-args# ~span-args
           ~span-sym  (apply span span-args#)]
       (try
         (with-open [_# (span-scope ~span-sym)]
           ~@body)
         (finally (.end ~span-sym))))))
