(ns crawler.datastore.database
    (:use 
      [clojure.tools.logging]
      [korma.core]
      [korma.db]
      [korma.config]))

; (defdb test-db (postgres 
                 ; {
                 ; :host "localhost"
                 ; :db "2ch"
                 ; :user "2ch"
                 ; :password "2ch"}))

(defentity bbs)

(defn- ins-key [m]
  (reduce (fn [m [x y]] (assoc m 
                          (keyword
                            (clojure.string/replace (name x) "-" "_"))
                             y)) {} m))

(defn- field-key [m]
  (reduce (fn [m [x y]] (assoc m 
                          (keyword
                            (clojure.string/replace (name x) "_" "-"))
                             y)) {} m))
(defentity threads
  (prepare ins-key)
  (transform field-key))

(defentity comments
  (prepare ins-key)
  (transform field-key))

(defn store-bbs [data]
  (transaction
    (debug data)
    (insert bbs
      (values data)))) 

(defn get-bbs []
  (select bbs)) 

(defn- get-rescount [{url :url}]
  (let [result (select threads (where {:url url}))]
    (if (> 0 (count result))
      (:res-count (first result))
      0)))

(defn store-thread [thread-info]
  (transaction
    (debug thread-info)
    (let [old-count (get-rescount thread-info)
         res-count (:res-count thread-info)] (if (> res-count old-count)
        (if (= old-count 0)
          ;; insert
          (insert threads (values thread-info))
          (update threads 
            (set-fields {:res-count res-count})
            (where {:url (:url thread-info)}))))
      old-count)))

(defn store-comments [data]
  (transaction
    (debug data)
    (insert comments (values data))))

; (insert-thread {:url "BC" :res-count 10})
; (def url "http://kohada.2ch.net/test/read.cgi/gamesrpg/1363928832/")
; (get-rescount {:url "http://ikura.2ch.net/test/read.cgi/food/1334832293/"})
; 
; 
; 

