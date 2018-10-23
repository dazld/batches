(ns batches.core-test
  (:require [clojure.test :refer :all]
            [spy.core :as spy]
            [spy.assert :as assert]
            [batches.core :as bc]))

(deftest multithreaded
  (testing "pushing from many threads"
    (let [futures 1000
          error-handler (spy/spy)
          result-handler (spy/spy)
          action (spy/spy (fn [v]
                            (reduce + 0 v)))
          foo (bc/accumulate action
                             10
                             error-handler
                             result-handler)
          push-value (fn []
                       (future
                         (bc/add foo 1)))]
      (dotimes [_ futures]
        (push-value))
      (Thread/sleep 500)
      (assert/called-with? result-handler futures)
      (assert/not-called? error-handler)
      (assert/called-at-least-once? action)
      (is (= 0 (bc/stop foo)))))

  (testing "booms are reported"
    (let [error-handler (spy/spy)
          result-handler (spy/spy)
          e (ex-info "nope" {:message "boom"})
          action (spy/spy (fn [v]
                            (if (>= (count v) 10)
                              (throw e)
                              :ok)))
          other (bc/accumulate action
                               10
                               error-handler
                               result-handler)]

      (assert/not-called? action)
      (bc/add other 1)
      (Thread/sleep 15)
      (assert/not-called? error-handler)
      (dotimes [n 10]
        (bc/add other n))
      (Thread/sleep 15)
      (assert/called-once? error-handler)
      (assert/called-with? action (range 10))
      (assert/called-with? error-handler e)
      (is (thrown? Throwable (bc/add other :pump)))
      (is (= :ok (bc/stop other)))))

  (testing "result handler"
    (let [error-handler (spy/spy)
          result-handler (spy/spy)
          foo (bc/accumulate identity
                             10
                             error-handler
                             result-handler)]
      (assert/not-called? result-handler)
      (bc/add foo 1)
      (assert/not-called? result-handler)
      (Thread/sleep 10)
      (assert/called-with? result-handler [1])

      (let [_ @(future (do
                         (Thread/sleep 150)
                         (bc/add foo 1)))]
        (assert/called-with? result-handler [1])))))

