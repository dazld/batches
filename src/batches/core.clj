(ns batches.core
  (:require [clojure.core.async :as a]))

(defprotocol Accumulating
  (add [this v])
  (stop [this]))

(defn accumulate
  "Periodically invoke action with values, which can be pushed into the queue with (add returned-from-accumulate value).
  Allows the invocation to be cancelled by calling (stop returned-from-accumulate)"
  [action time-ms on-error on-result]
  (let [stop (a/chan)
        push (a/chan)
        try-action (fn [v]
                     (try
                       (when (seq v)
                         (on-result (action v)))
                       (catch Throwable e
                         (a/put! stop :stop)
                         (on-error e))))
        main-loop (a/go-loop [vals []
                              timer (a/timeout time-ms)]
                    (let [[v ch] (a/alts! [push timer stop] :priority true)]
                      (condp = ch
                        push (recur (conj vals v) timer)
                        timer (do
                                (a/thread (try-action vals))
                                (recur [] (a/timeout time-ms)))
                        stop (do
                               (a/close! push)
                               (action vals)))))]
    (reify
      Accumulating
      (add [_ v]
        (let [result (a/put! push v)]
          (when (not (true? result))
            (throw (ex-info "Trying to push to a stopped accumulator" {})))))
      (stop [_]
        (a/put! stop :stop)
        (a/<!! main-loop)))))
