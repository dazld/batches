(ns batches.core-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :as a]
            [spy.core :as spy]
            [spy.assert :as assert]
            [batches.core :as bc]))

(deftest batches
  (testing "pushing many values"
    (let [in (a/chan 2000)
          out (a/chan)
          foo (bc/accumulate 600 in out)
          then (System/currentTimeMillis)]

      (doseq [n (range 10000)]
        (a/>!! in 1))

      (let [result (reduce + 0 (a/<!! out))
            elapsed (- (System/currentTimeMillis) then)]
        (println "Expected around 600ms, took:" (str elapsed "ms"))
        (is (> elapsed 500))
        (is (= result 10000))
        (is (= [] (a/<!! out))))))

  (testing "pushing `:stop` will stop recursion and drain any pending values directly into out"
    (let [in (a/chan)
          out (a/chan)
          foo (bc/accumulate 5000 in out)
          then (System/currentTimeMillis)]

      (a/>!! in :foo)
      (a/>!! in :stop)
      (let [result (a/<!! out)
            elapsed (- (System/currentTimeMillis) then)]
        (is (= result [:foo]))
        (is (<= elapsed 1))))))

