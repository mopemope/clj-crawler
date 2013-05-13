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

  (store-bbs [datastore board-info])

  (get-bbs [datastore])

  (store-thread [datastore thread-info])
  
  (store-comments [datastore comment-info])) 

(defrecord Database [config]

  Repository
  (setup-store [datastore]
    (defdb db (postgres (:config datastore))))
         
  (store-bbs [datastore board-info]
    (db/store-bbs board-info))

  (get-bbs [datastore]
    (db/get-bbs))

  (store-thread [datastore thread-info]
    (db/store-thread thread-info))
  
  (store-comments [datastore comment-info]
    (db/store-comments comment-info)))

(defn make-db-config [config]
  (Database. config))
; (map #(println %) (.getInterfaces Database))
; (setup-store (make-db-config {}))
