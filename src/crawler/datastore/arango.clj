(ns crawler.datastore.arango
  (:require
   [taoensso.timbre :as timbre]
   [clarango.core :as clacore]
   [clarango.database :as database]
   [clarango.document :as document]
   [clarango.collection :as collection]
   [clarango.query :as query]
   [clarango.index :as index]))

(timbre/refer-timbre)

(defn- get-collection-info [name]
  (try
    (collection/get-info name)
    (catch Exception e)))

(defn setup-db [datastore]
  (clacore/set-connection! (:config datastore))

  ;; create collections
  (when-not (get-collection-info "bbs")
    (collection/create "bbs"))

  (when-not (get-collection-info "threads")
    (collection/create "threads")
    (index/create
     {:type "fulltext" :fields ["title"]} "threads"))
  
  (when-not (get-collection-info "comments")
    (collection/create "comments")
    (index/create
     {:type "fulltext" :fields ["comment"]} "comments")))

(defn- get-thread-key [thread-info]
  (let [word (clojure.string/split (:url thread-info) #"/")
        [nm id] (take-last 2 word)]
    (str nm "_"  id)))

(defn- get-comment-key [comment-info]
  (let [word (clojure.string/split (:url comment-info) #"/")
        [nm id] (take-last 2 word)
        no (:no comment-info)]
    (str nm "_"  id "_" no)))

(defn store-bbs [data]
  (clacore/with-collection "bbs"
    (document/create-with-key data (:url data))))

(defn get-bbs []
  (clacore/with-collection "bbs"
   (query/execute "FOR b IN bbs RETURN b"))) 

(defn- get-rescount [{url :url}]
  (let [res (clacore/with-collection "threads"
                 (query/execute "FOR t IN threads RETURN LENGTH(t) "))
        result (:result res)]
    (if (and result (> result 0))
      result
      0)))

(defn store-thread [thread-info]
  (let [old-count (get-rescount thread-info)
        res-count (:res-count thread-info)]
    (debug (format "store-thread thread-info:%s old-count:%s res-count:%s" thread-info old-count res-count))
    (if (> res-count old-count)
      (if (= old-count 0)
        (do
          (debugf "insert thread-info %s %s" thread-info (:url thread-info))
          (clacore/with-collection "threads"
            (document/create-with-key thread-info (get-thread-key thread-info))))
        (do
          (debugf "update thread-info %s" thread-info)
          (clacore/with-collection "threads"
            (document/replace-by-key thread-info (get-thread-key thread-info))))))
    old-count))

(defn store-comments [datas]
  (doseq [data datas]
;;    (debug data)
    (clacore/with-collection "comments"
      (document/create-with-key data (get-comment-key data)))))

(defn store-thread-comments [thread-info comment-info]
  (let [old-cnt (store-thread thread-info)]
    (if (>= old-cnt 0)
      (let [ins (drop old-cnt comment-info)]
        (if (> (count ins) 0)
          (do
            (store-comments ins)
            (infof "stored:%s:%s" (:title thread-info) thread-info))
          (infof "進捗なし:%s" (:title thread-info))))
      (infof "進捗なし:%s" (:title thread-info)))))

