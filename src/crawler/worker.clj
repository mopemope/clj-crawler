(ns crawler.worker
  (:require
   [clojure.core.async :as async :refer [<! >! >!! <!! timeout chan alts! alts!! go go-loop sliding-buffer]]
   [taoensso.timbre :as timbre]
   ))

(timbre/refer-timbre)

(def ^:dynamic *sleep-time* (* 1000 25))

(def ^:private mark 'dummy)

(defn run-worker [f cin cout]
  (go
    (while true
      (when-let [[data _] (alts! [cin])]
        (try
          (debugf "start call worker val:%s" data)
          (f data)
          (debugf "end   call worker val:%s" data)
          (debugf "wait:%s ... " *sleep-time*)
          (alts! [(timeout *sleep-time*)])
          (>! cout data)
          (catch Exception e
            (do
              (error e)
              (>! cout e))))))))

(defn- make-chan []
  [(chan 1) (chan 1)])

(defn start-worker [queue fn nth]
  (let [mainc (chan)
        chans (vec (repeatedly nth make-chan))
        queue (vec queue)]
    (doseq [[cin cout] chans]
      (run-worker fn cin cout))
    (go
      (loop [q queue waiter [] cs chans]
        (let [value (peek q)
              [cin cout] (peek cs)]
          (if (and value cin)
            (do
              (>! cin value)
              (recur (pop q) (conj waiter cout) (pop cs)))
            (let [len (count waiter)]
              (when (> len 0)
                (dotimes [n len]
                  (let [[v _] (alts! waiter)]
                    (debugf "fin %s" v)))
                (when q
                  (recur q [] chans)))))))
      (>! mainc "FIN"))
    (let [[v c] (alts!! [mainc])]
      (debug v))))

