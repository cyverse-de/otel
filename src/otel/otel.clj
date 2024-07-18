(ns otel.otel
  (:import [io.opentelemetry.api GlobalOpenTelemetry]
           [io.opentelemetry.api.common AttributeKey]
           [io.opentelemetry.api.trace Span SpanKind Tracer]
           [io.opentelemetry.context Context Scope]))

(defn ^Tracer tracer
  "Set up a tracer using the OpenTelemetry API"
  []
  (let [tracer-provider (GlobalOpenTelemetry/getTracerProvider)]
    (.get tracer-provider "otel.otel clojure")))

(def span-kinds
  {:internal SpanKind/INTERNAL
   :server   SpanKind/SERVER
   :client   SpanKind/CLIENT
   :producer SpanKind/PRODUCER
   :consumer SpanKind/CONSUMER})

(def default-opts
  {:kind :internal})


(defn ^Span current-span [] (Span/current))

(defn ^Span span
  "Create a span. The optional `opts` can set various properties on the span,
  and when not provided uses `default-opts`."
  ([span-name]
   (span span-name default-opts))
  ([span-name opts]
   (let [{:keys [kind link-spans attributes]} (merge default-opts opts)
         builder (->
           (.spanBuilder (tracer) span-name)
           (.setSpanKind (span-kinds kind))
           (.setAttribute "thread.id" (.threadId (Thread/currentThread)))
           (.setAttribute "thread.name" (.getName (Thread/currentThread))))]
     (when (seq attributes)
       (doall (map (fn [[k v]] (.setAttribute builder ^AttributeKey k ^Object v)) attributes)))
     (when (seq link-spans)
       (.setParent builder (current-span))
       (doall (map (fn [^Span s] (.addLink builder (.getSpanContext s))) link-spans)))
     (.startSpan builder))))

(defn ^Scope span-scope
  "Open a Scope for a Span, to be used with with-open"
  [span]
  (.makeCurrent (.with (Context/current) span)))

(defn end-span
  "Simply calls .end on a span. This was being used to fix an unresolvable field reference warning."
  [^Span span]
  (.end span))

(defmacro with-span
  [span-binding & body]
  (let [[span-sym span-args] span-binding]
    `(let [span-args# ~span-args
           ~span-sym  (apply span span-args#)]
       (try
         (with-open [_# (span-scope ~span-sym)]
           ~@body)
         (finally (end-span ~span-sym))))))
