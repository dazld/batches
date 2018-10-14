(ns batches.core
  (:require [clojure.core.async :as a]))

(defprotocol Accumulating
  (add [this v])
  (stop [this]))

(defn periodically
  "Invoke fn every ms while the continue channel is open. Return the continue channel
  allowing the invocation to be cancelled by closing that channel from outside."
  [fn ms]
  (let [continue (a/chan)]
    (a/put! continue 1)
    (a/go-loop []
        (a/<! continue)
        (a/<! (a/timeout ms))
        (try
          (fn)
          (catch Throwable err
            (println "boom when invoking" err)
            (a/close! continue)))
        (when (a/put! continue 1)
          (recur)))
    continue))

(defn- drain
  "Take all values from atom `store`, invoke f with the value as first arg, and reset the
  store to an empty vector once done."
  [f store]
  (let [vals @store]
    (try
      (f vals)
      (catch Throwable err
        (println "boom when draining" err)))
    (reset! store [])))

(defn make-store
  [initial]
  (atom initial))

(defn accumulate
  "Periodically invoke action with values, which can be conj'd onto the store with add. Allows the invocation to be cancelled´´
  by calling (stop (accumulate identity))"
  [action time-ms]
  (let [values (make-store [])
        continue (periodically (partial drain action values) time-ms)]
    (reify
      Accumulating
        (add [_ v]
          (swap! values conj v))
        (stop [_] (a/close! continue)))))
