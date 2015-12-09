# pacer

A Clojure library designed to pace at certain TPS.

## Usage

```clojure
(use 'pacer.core)
```
Define the recipient channel
```clojure
(def c (chan))
```
What the recipient channel should do when it receives data
```clojure
(go-loop [] (println (<! c)) (recur))
```

Pace 10 times a 100 tps into channel c
```clojure
(pacer 100 10 (fn [v] v) c)
```

## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
