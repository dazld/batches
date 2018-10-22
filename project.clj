(defproject com.flarework/batches "0.1.0-SNAPSHOT"
  :description "Accumulate values into an atom and periodically act on them"
  :url "https://flarework.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [tortue/spy "1.4.0"]
                 [org.clojure/core.async "0.4.474"]]
  :aliases {"rebl" ["trampoline" "run" "-m" "rebel-readline.main"]})
