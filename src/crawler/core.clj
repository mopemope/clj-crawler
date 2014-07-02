(ns crawler.core
  (:use 
    [crawler.crawler]
    [crawler.datastore.datastore]))

(def store-info 
  (make-arango-config
   {
    :connection-url "http://localhost:8529/"
    :db-name "ma2ch"
    }))

(defn -main []
  (let [store store-info]
    (start-crawl-all store 2)))

