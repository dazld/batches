
[![CircleCI](https://circleci.com/gh/dazld/batches/tree/master.svg?style=svg)](https://circleci.com/gh/dazld/batches/tree/master)
[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.dazld/batches.svg)](https://clojars.org/org.clojars.dazld/batches)

# batches

A Clojure library designed to accumulate values from an input channel and periodically provide them to an output channel.

#### Clojars coords

`[org.clojars.dazld/batches "0.1.0"]`

## Usage
```clojure
=> (require '[clojure.core.async :as ac])
=> (require '[batches.core :refer [accumulate]])
=> (def in (ac/chan))
=> (def out (ac/chan))
=> (def foo (accumulate 3000 in out)))
=> (doseq [v (range 10)]
     (ac/put! in 1))
=> (reduce + 0 (ac/<!! out)))
10
; stop the accumulator
=> (and (ac/>!! in :stop) (ac/<!! out))
[]

```

## License

Copyright © 2018 Dan Peddle

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
