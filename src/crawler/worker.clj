(ns crawler.worker
    (:use 
      [clojure.tools.logging]))

(def ^:dynamic *sleep-time* 5000)

(defn dequeue! [queue]
  (loop []
    (let [q @queue
          value (first q)
          nq (next q)]
      (if (compare-and-set! queue q nq)
        value
        (recur)))))

(defn- make-worker [f]
  (let [worker-fn f]
   (fn [q] 
      (when-let [val (dequeue! q)]
        (try
          (debug (format "start call worker val:%s" val))
          (worker-fn val)
          (debug (format "end call worker val:%s remain:%s" val (count @q)))
          (catch Exception e (error e)))
        (debug (format "wait:%s ... " *sleep-time*))
        (Thread/sleep *sleep-time*)
        (recur q)))))

(defn start-worker [q f nthread]
  (let [p (promise)
        cnt (atom nthread)]
    (dotimes [i nthread]
      (future 
        ((make-worker f) q)
        (if (= 0 (swap! cnt dec))
          (deliver p "OK"))))
    p))

