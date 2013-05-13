(ns crawler.datastore.datastore
  (:require
    [crawler.datastore.database :as db])
  (:use 
      [clojure.tools.logging]
      [korma.core]
      [korma.db]
      [korma.config]))

(defprotocol Repository

  (setup-store [datastore])
  
  (get-bbs [datastore])

  (store-bbs [datastore board-info])

  (store-thread [datastore thread-info])
  
  (store-comments [datastore comment-info])

  (store-thread-comments [datastore thread-info comment-info])) 

(defrecord Database [config]

  Repository
  (setup-store [datastore]
    (defdb db (postgres (:config datastore))))
  
  (get-bbs [datastore]
    (db/get-bbs))
         
  (store-bbs [datastore board-info]
    (db/store-bbs board-info))

  (store-thread [datastore thread-info]
    (db/store-thread thread-info))
  
  (store-comments [datastore comment-info]
    (db/store-comments comment-info))

  (store-thread-comments [datastore thread-info comment-info]
    (db/store-thread-comments thread-info comment-info)))

(defn make-db-config [config]
  (Database. config))
; (map #(println %) (.getInterfaces Database))
; (setup-store (make-db-config {}))
