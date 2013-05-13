(ns crawler.core
  (:use 
    [crawler.crawler]
    [crawler.datastore.datastore]))

(def store-info 
     (make-db-config 
       { :host "localhost" :db "2ch" :user "2ch" :password "2ch"}))

(defn -main []
  (let [store store-info]
    ; (store-bbs-info store {:url "http://anago.2ch.net/software/" :title "ソフトウェア"})
    ; (start-crawl store 3)
    (start-crawl-all store 3)))

