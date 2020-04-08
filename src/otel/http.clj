(ns otel.http
  (:import [io.opentelemetry.context.propagation HttpTextFormat$Getter HttpTextFormat$Setter]))

(defn ring-getter
  []
  (reify HttpTextFormat$Getter
    (get [this req k]
      (if (contains? (:headers req) k)
        (first (get (:headers req) k))
        nil))))
