(ns batches.core-test
  (:require [clojure.test :refer :all]
            [batches.core :as bc]))


(deftest multithreaded
  (let [futures 500
        result (atom [])
        foo (bc/accumulate (fn [v]
                             (swap! result
                                    (fn [results]
                                      (conj results (reduce + 0 v)))))
                           100)
        worker (fn []
                 (future
                   (bc/add foo 1)))]
    (dotimes [_ futures]
      (worker))
    (Thread/sleep 100)
    (is (= (first @result) futures))
    (dotimes [_ futures]
      (worker))
    (Thread/sleep 100)
    (is (= (first @result) futures))
    (bc/stop foo)))


(deftest booms
  (let []))