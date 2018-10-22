(ns batches.core-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :as a]
            [spy.core :as spy]
            [spy.assert :as assert]
            [batches.core :as bc]))

(deftest multithreaded
  (testing "pushing from many threads"
    (let [futures 1000
          error-handler (spy/spy)
          action (spy/spy (fn [v]
                            (reduce + 0 v)))
          foo (bc/accumulate action
                             10
                             error-handler)
          push-value (fn []
                       (future
                         (bc/add foo 1)))]
      (dotimes [_ futures]
        (push-value))
      (is (= (a/<!! (bc/results foo))) futures)
      (assert/not-called? error-handler)
      (assert/called-once? action)
      (is (= 0 (bc/stop foo)))))

  (testing "booms are reported"
    (let [error-handler (spy/spy)
          e (ex-info "nope" {:message "boom"})
          action (spy/spy (fn [v]
                            (if (>= (count v) 10)
                              (throw e)
                              :ok)))
          other (bc/accumulate action
                               10
                               error-handler)]
      (is (= (a/<!! (bc/results other))) nil)
      (assert/called-once? action)
      (bc/add other 1)
      (Thread/sleep 20)
      (assert/not-called? error-handler)
      (dotimes [n 10]
        (bc/add other n))
      (Thread/sleep 20)
      (assert/called-once? error-handler)
      (assert/called-with? action (range 10))
      (assert/called-with? error-handler e)
      (bc/add other :pump)
      (is (= :ok (bc/stop other))))))




