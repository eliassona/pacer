# pacer

A Clojure library designed to pace at certain TPS.

## Usage

```clojure
(use 'pacer.core)
```
Define the recipient channel
```clojure
(require '[clojure.core.async :refer [chan go-loop <!]])
(def c (chan))
```
What the recipient channel should do when it receives data
```clojure
=> (go-loop [] (println {:timestamp (System/currentTimeMillis), :value(<! c)}) (recur))
#object[clojure.core.async.impl.channels.ManyToManyChannel 0xcb0aae0 "clojure.core.async.impl.channels.ManyToManyChannel@cb0aae0"]

```

Pace 10 times a 100 tps into channel c
```clojure
=> (pacer 100 10 (fn [v] v) c)
#object[clojure.lang.Atom 0x6df43fb0 {:status :ready, :val 10000000}]
0
1
2
user=> 3
4
5
6
7
8
9

```

## License

Copyright © 2015 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
