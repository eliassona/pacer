(ns pacer.core
  (:require 
    [clojure.core.async :refer [chan go >! <! >!! <!! go-loop alts! timeout onto-chan pipeline close! sliding-buffer]]))

(defn current-time [] (java.lang.System/nanoTime))



(defn ns->ms [ns] (max 1 (int (/ ns 1E6))))
(defn tps->ns [tps] (if (= tps 0) 0 (int (* 1E9 (double (/ 1 tps))))))
(defn ns->tps [ns] (long (/ 1 (/ ns 1E9))))

(defn calc-new-delay [timeout? tps step-in-ns]
  (let [v 
        (if timeout? 
          step-in-ns
          (tps->ns tps)
          )]
    [v (ns->ms v)]))

(def ramp-items 100)

(defn ramp [v i]
  v
  #_(if (> i ramp-items)
     v
     (let [tps (ns->tps v)]
       (tps->ns (long (* (/ i ramp-items) tps))))))


(defn pacer 
  "Put data in a channel at a certain pace
   tps: number of transactions per second
   n:   the number of transactions
   the-fn: this function take one arg which is an int and should return data that is paced
   channel: the channel that the paced data is put into
   returns an atom containing the pace interval in nano seconds
  "
  [tps n the-fn channel]
  (let [step-in-ns (atom (tps->ns tps))
        sleep-time-in-ms (max 1 (int (/ @step-in-ns 1E6)))]
    (go-loop [i 0
              last-time-in-ns (current-time)]
      (when (< i n)
        (let [curr-time-in-ns (current-time)
              s-in-ns (ramp @step-in-ns i)]
          (if (> (- curr-time-in-ns last-time-in-ns) s-in-ns)
            (do 
              (when 
                (>! channel (the-fn i)) 
                (recur (inc i) (+ last-time-in-ns s-in-ns))))
            (do
              (Thread/sleep sleep-time-in-ms)
              (recur i last-time-in-ns))
              ))))
    step-in-ns
    ))

(defn set-tps! 
  "set the pace in tps
  tps: transactions per second
  pace: the atom containing the pace interval in nanoseconds"
  [tps pace]
  (reset! pace (tps->ns tps)))


(defn rnd-tps! 
  "Change the tps at random
   max-tps: the max transaction per second
   pace: the atom containing the pace interval in nanoseconds
   returns a channel for stopping this process, i.e (>!! returned-chan :stop)
  "
  [max-tps pace]
  (let [stop-chan (chan)]
    (go-loop 
      []
      (let [[_ c] (alts! [stop-chan (timeout (max 1000 (-> 10000 rand long)))])] 
        (when (not= c stop-chan)
          (let [tps (-> max-tps rand long)]
            (set-tps! tps pace)
            (println (str "tps: " tps))
            (recur)))))
    stop-chan))