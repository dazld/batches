(ns batches.core
  (:require [clojure.core.async :as a]
            [clojure.tools.logging :as log]))

(defprotocol Accumulating
  (add [this v])
  (stop [this]))

(defn accumulate
  "Periodically invoke action with values, which can be conj'd onto the store with add. Allows the invocation to be cancelled´´
  by calling (stop (accumulate identity))"
  [action time-ms]
  (let [stop (a/chan 1)
        push (a/chan)
        timer (a/chan)
        _    (a/go-loop []
               (a/<! (a/timeout time-ms))
               (a/>! timer :go)
               (recur))]
    (a/go-loop [vals []]
      (let [[v ch] (a/alts! [push timer stop] :priority true)]
        (condp = ch
          push (recur (conj vals v))
          timer (do
                  (action vals)
                  (recur []))
          stop vals)))
    (reify
      Accumulating
        (add [_ v] (a/put! push v))
        (stop [_] (a/put! stop :stop)))))
