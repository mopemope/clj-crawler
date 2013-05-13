(ns crawler.core
  (:use 
    [crawler.crawler]
    [crawler.datastore.datastore]))

(def store-info 
     (make-db-config 
       { :host "localhost" :db "2ch" :user "2ch" :password "2ch"}))

(defn -main []
  (let [store store-info]
    (start-crawl store 2)))
