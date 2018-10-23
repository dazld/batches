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
          foo (bc/accumulate 150 in out)]

      (doseq [n (range 10000)]
        (a/>!! in 1))

      (let [then (System/currentTimeMillis)
            result (reduce + 0 (a/<!! out))
            elapsed (- (System/currentTimeMillis) then)]
        (is (> elapsed 100))
        (is (= result 10000))
        (is (= [] (a/<!! out))))))

  (testing "pushing `:stop` will stop recursion and drain any pending values directly into out"
    (let [in (a/chan)
          out (a/chan)
          foo (bc/accumulate 5000 in out)]

      (a/>!! in :foo)
      (a/>!! in :stop)
      (let [then (System/currentTimeMillis)
            result (a/<!! out)
            elapsed (- (System/currentTimeMillis) then)]
        (is (= result [:foo]))
        (is (<= elapsed 1))))))

