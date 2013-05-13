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
    (insert bbs
      (values data)))) 

(defn get-bbs []
  (select bbs)) 

(defn- get-rescount [{url :url}]
  (let [result (select threads (where {:url url}))]
    (if (> (count result) 0)
      (:res-count (first result))
      0)))

(defn store-thread [thread-info]
  (let [old-count (get-rescount thread-info)
       res-count (:res-count thread-info)]
    (debug (format "store-thread thread-info:%s old-count:%s res-count:%s" thread-info old-count res-count))
    (if (> res-count old-count)
      (if (= old-count 0)
        (do
          (debug (format "insert thread-info %s" thread-info))
          (insert threads (values thread-info)))
        (do
          (debug (format "update thread-info %s" thread-info))
          (update threads 
            (set-fields {:res-count res-count})
            (where {:url (:url thread-info)})))))
    old-count))

(defn store-comments [data]
  (insert comments (values data)))

(defn store-thread-comments [thread-info comment-info]
  (transaction
    (let [old-cnt (store-thread thread-info)]
      (if (>= old-cnt 0)
        (let [ins (drop old-cnt comment-info)]
          (if (> (count ins) 0)
            (do
              (store-comments ins)
              (info (format "stored:%s:%s" (:title thread-info) thread-info)))
            (info (format "進捗なし:%s" (:title thread-info)))))
        (info (format "進捗なし:%s" (:title thread-info)))))))
  
; (insert-thread {:url "BC" :res-count 10})
; (def url "http://kohada.2ch.net/test/read.cgi/gamesrpg/1363928832/")
; (get-rescount {:url "http://ikura.2ch.net/test/read.cgi/food/1334832293/"})
; 
; 
; 

