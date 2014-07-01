(ns crawler.worker
  (:require
   [clojure.core.async :as async :refer [<! >! >!! <!! timeout chan alts! alts!! go sliding-buffer]]
   [taoensso.timbre :as timbre]
   ))

(timbre/refer-timbre)

(def ^:dynamic *sleep-time* (* 1000 30))

(defn run-worker [f c]
  (go
    (while true
      (when-let [val (<! c)]
        (try
          (debugf "start call worker val:%s" val)
          (f val)
          (debugf "end   call worker val:%s" val)
          (debugf "wait:%s ... " *sleep-time*)
          (alts! [(timeout *sleep-time*)])
          (>! c val)
          (catch Exception e
            (do
              (error e)
              (>! c e))))))))

(defn- make-chan []
  (chan (sliding-buffer 1)))

(defn start-worker [queue fn nth]
  (let [mainc (chan)
        cs (vec (repeatedly nth make-chan))
        queue (vec queue)]
    (doseq [c cs]
      (run-worker fn c))
    (go
      (loop [queue queue ccs cs]
        (if-let [c (peek ccs)]
          (do 
            (debugf "q:%s cs:%s" (count queue) (count ccs))
            (if-let [val (peek queue)]
              (do
                (>! c val)
                (recur (pop queue) (pop ccs)))
              (recur queue [])))
          (do
            (dotimes [i nth]
              (let [[v c] (alts! cs)]
                (debugf "chan fin %s %s" v c)))
            (when queue
              (recur queue cs)))))
      (>! mainc "FIN"))
    (let [[v c] (alts!! [mainc])]
      (debug v))))
