(ns otel.http
  (:require [clojure.string :as string])
  (:import [io.opentelemetry.context.propagation HttpTextFormat$Getter HttpTextFormat$Setter]))

(defn ring-getter
  []
  (reify HttpTextFormat$Getter
    (get [this req k]
      (if (contains? (:headers req) k)
        (first (string/split (get (:headers req) k) #",")) ;; ring handles multiple headers with comma-joining, and at least traceparent can't have commas in it
        nil))))
