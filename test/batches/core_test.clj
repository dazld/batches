(ns batches.core-test
  (:require [clojure.test :refer :all]
            [batches.core :as bc]))


(deftest multithreaded
  (let [threads 10000
        result (atom [])
        foo (bc/accumulate (fn [v]
                             (swap! result
                                    (fn [results]
                                      (conj results (reduce + 0 v)))))
                           100)
        worker (fn []
                 (future
                   (bc/add foo 1)))]
    (dotimes [_ threads]
      (worker))
    (Thread/sleep 100)
    (bc/stop foo)
    (is (= (first @result) threads))))


