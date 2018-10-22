(ns batches.core
  (:require [clojure.core.async :as a]))

(defprotocol Accumulating
  (add [this v])
  (results [this])
  (stop [this]))

(defn accumulate
  "Periodically invoke action with values, which can be pushed into the queue with (add returned-from-accumulate value).
  Allows the invocation to be cancelled by calling (stop returned-from-accumulate)"
  [action time-ms on-error]
  (let [stop (a/chan 1)
        push (a/chan 128)
        timer (a/chan)
        results (a/chan 128)
        errors (a/chan 128)
        try-action (fn [v]
                     (try
                      (a/put! results (action v))
                      (catch Throwable e
                        (a/put! stop :stop)
                        (a/put! errors e))))
        main-loop (a/go-loop [vals []]
                    (let [[v ch] (a/alts! [push timer stop] :priority true)]
                      (condp = ch
                        push (recur (conj vals v))
                        timer (do
                                (a/thread (try-action vals))
                                (recur []))
                        stop (action vals))))]
    (a/go-loop []
      (a/<! (a/timeout time-ms))
      (a/>! timer :go)
      (recur))
    (a/go-loop []
      (on-error (a/<! errors))
      (recur))
    (reify
      Accumulating
      (add [_ v] (a/put! push v))
      (results [_] results)
      (stop [_]
        (a/put! stop :stop)
        (a/<!! main-loop)))))
