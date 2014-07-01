(ns crawler.datastore.datastore
  (:require
   [taoensso.timbre :as timbre]
   [crawler.datastore.database :as db]
   [crawler.datastore.arango :as arango]
   ))

(timbre/refer-timbre)

(defprotocol Repository

  (setup-store [config])
  
  (get-bbs [datastore])

  (store-bbs [datastore board-info])

  (store-thread [datastore thread-info])
  
  (store-comments [datastore comment-info])

  (store-thread-comments [datastore thread-info comment-info])) 

(defrecord Database [config]

  Repository
  (setup-store [datastore]
    (db/setup-db (:config datastore)))
  
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

(defrecord Arango [config]

  Repository
  (setup-store [datastore]
    (arango/setup-db datastore))
  
  (get-bbs [datastore]
    (arango/get-bbs))
  
  (store-bbs [datastore board-info]
    (arango/store-bbs board-info))

  (store-thread [datastore thread-info]
    (arango/store-thread thread-info))
  
  (store-comments [datastore comment-info]
    (arango/store-comments comment-info))

  (store-thread-comments [datastore thread-info comment-info]
    (arango/store-thread-comments thread-info comment-info)))

(defn make-db-config [config]
  (Database. config))

(defn make-arango-config [config]
  (Arango. config))
