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
          (debug (format "start worker %s" val))
          (worker-fn val)
          (debug (format "remain %s" (count @q)))
          (Thread/sleep *sleep-time*)
          (catch Exception e (error e)))
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

; (start-worker (atom (vec (range 10))) #(println %) 2)
;
