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
          foo (bc/accumulate 10 in out)]

      (doseq [n (range 10000)]
        (a/>!! in 1))

      (is (= 10000 (reduce + 0 (a/<!! out))))
      (is (= [] (a/<!! out)))))

  (testing "pushing `:stop` will stop recursion and drain any pending values"
    (let [in (a/chan)
          out (a/chan)
          foo (bc/accumulate 5000 in out)]

      (a/>!! in :foo)
      (a/>!! in :stop)

      (is (= [:foo] (a/<!! out))))))

