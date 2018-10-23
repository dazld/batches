(defproject org.clojars.dazld/batches "0.1.0"
  :description "Accumulate values from an input channel and periodically push them to a core async channel"
  :url "https://github.com/dazld/batches"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [tortue/spy "1.4.0"]
                 [org.clojure/core.async "0.4.474"]])
