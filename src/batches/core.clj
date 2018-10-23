(ns batches.core
  (:require [clojure.core.async :as a]))

(defn accumulate
  "Periodically invoke action with values, which can be pushed into the queue with (add returned-from-accumulate value).
  Allows the invocation to be cancelled by calling (stop returned-from-accumulate)"
  [time-ms in out]
  (a/go-loop [vals []
              timer (a/timeout time-ms)]
    (let [[v ch] (a/alts! [in timer] :priority true)]
      (condp identical? ch
        in (if (= v :stop)
             (a/>! out vals)
             (recur (conj vals v) timer))
        timer (do
                (a/>! out vals)
                (recur [] (a/timeout time-ms)))))))



