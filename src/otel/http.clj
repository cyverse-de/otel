(ns otel.http
  (:require [clojure.string :as string])
  (:import [io.opentelemetry.context.propagation TextMapGetter]))

(defn ring-getter
  []
  (reify TextMapGetter
    (get [this req k]
      (if (contains? (:headers req) k)
        (first (string/split (get (:headers req) k) #",")) ;; ring handles multiple headers with comma-joining, and at least traceparent can't have commas in it
        nil))))
