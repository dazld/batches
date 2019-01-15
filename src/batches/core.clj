(ns batches.core
  (:require [clojure.core.async :as a]
            [clojure.tools.logging :as log]))

(defn accumulate
  "Every `time-ms`, take the values accumulated inside a go-loop from `in` and push them to `v` as a collection.
  closing the `in` channel will stop accumulation and drain any pending values to `out`."
  [time-ms in out]
  (a/go-loop [vals []
              timer (a/timeout time-ms)]
    (let [[v ch] (a/alts! [in timer] :priority true)]
      (condp identical? ch
        in (if (nil? v)
             (do
               (log/info (str "Accumulator closed, draining " (count vals) " to out"))
               (a/>! out vals))
             (recur (conj vals v) timer))
        timer (do
                (a/>! out vals)
                (recur [] (a/timeout time-ms)))))))


