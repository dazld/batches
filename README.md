# batches

A Clojure library designed to accumulate values and periodically invoke (probably for side effects) a function with those values

## VERY ALPHA

don't use this yet. really. mostly written for a learning experience... 

## Usage
```clojure
=> (def action (fn [value] (println (count values)))
=> (def foo (batches/accumulate action 3000)))
=> (doseq [v (range 10)]
     (add foo v))
; (around 3s after starting, should see 10 or so, then 0 every 3s while not adding new ints)

; schedule stopping the accumulator
=> (stop foo)

```

## License

Copyright © 2018 Dan Peddle

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
