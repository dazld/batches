(ns batches.core
  (:require [clojure.core.async :as a]
            [clojure.tools.logging :as log]))

(defprotocol Accumulating
  (add [this v])
  (stop [this]))

(defn accumulate
  "Periodically invoke action with values, which can be pushed into the queue with (add this value).
  Allows the invocation to be cancelled by calling (stop (accumulate identity))"
  [action time-ms on-error]
  (let [stop (a/chan 1)
        push (a/chan 128)
        timer (a/chan)
        try-action (fn [v]
                     (try
                      (action v)
                      (catch Throwable e
                        (on-error e)
                        (a/put! stop e))))
        _ (a/go-loop []
            (a/<! (a/timeout time-ms))
            (a/>! timer :go)
            (recur))
        main-loop (a/go-loop [vals []]
                    (let [[v ch] (a/alts! [push timer stop] :priority true)]
                      (condp = ch
                        push (recur (conj vals v))
                        timer (do
                                (a/thread (try-action vals))
                                (recur []))
                        stop (do
                               (action vals)
                               v))))]
    (reify
      Accumulating
      (add [_ v] (a/put! push v))
      (stop [_]
        (a/put! stop :stop)
        (a/<!! main-loop)))))
