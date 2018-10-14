(ns batches.core-test
  (:require [clojure.test :refer :all]
            [batches.core :as bc]))


(deftest multithreaded
  (let [futures 1000
        wait 200
        result (atom [])
        action (fn [v]
                 (swap! result
                        (fn [results]
                          (conj results (reduce + 0 v)))))
        foo (bc/accumulate action
                           wait)
        worker (fn []
                 (future
                   (bc/add foo 1)))]
    (dotimes [_ futures]
      (worker))
    (Thread/sleep wait)
    (is (= (first @result) futures))
    (dotimes [_ futures]
      (worker))
    (Thread/sleep wait)
    (is (= (first @result) futures))
    (bc/add foo 1)
    (is (= (bc/stop foo) [1]))))


(deftest booms
  (let []))