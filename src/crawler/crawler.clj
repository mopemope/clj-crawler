(ns crawler.crawler
  (import 
   (java.util.regex Pattern))
  (:require
   [taoensso.timbre :as timbre]
   [crawler.datastore.datastore :as store])
  (:use 
   [crawler.worker]
   [crawler.parser]))

(timbre/refer-timbre)

(def ^:dynamic *base-url* "http://menu.2ch.net/bbsmenu.html")

(def ^:private bg20-url "http://bg20.2ch.net/test/r.so/")

(def ^:private url-re (Pattern/compile ".*/(\\d+)/.*" Pattern/DOTALL))

(def ^:dynamic *thread-num* 1)

(def ^:dynamic *datastore*)

(def ^:dynamic *use-cache* false)

(defn- get-dat-url [board-url url]
  (let [[_ index] (re-find (re-matcher url-re url))]
    (if *use-cache*
      (format "%s%s%s/" bg20-url (subs board-url 7) index)
      (format "%sdat/%s.dat" board-url index))))

(defn get-board-list []
  (let [data (slurp *base-url* :encoding "MS932")]
    (debug "get-board-list")
    (parse-menu data)))

(defn- get-thread-list [board-nm board-url]
  (let [url (str board-url "subback.html")
        data (slurp url :encoding "MS932")]
    (debugf "get thread list title:'%s' url:'%s'" board-nm board-url)
    (parse-thread-list board-url data)))

(defn- get-thread-data [thread-info]
  (let [url (get-dat-url (:board-url thread-info) (:url thread-info))]
    (debugf "load dat from url:%s" url)
    (let [^String data (slurp url :encoding "MS932")]
     (if (= (.indexOf data "ERROR = 5656") -1)
       (parse-thread thread-info data)
       (infof "error load url:%s" url)))))

(defn- crawl-thread [thread-info]
  (do
    (infof "start thread %s" (:title thread-info))
    (if-let [comment-info (get-thread-data thread-info)]
      (do
        (store/store-thread-comments *datastore* thread-info comment-info)
        (infof "end thread %s" (:title thread-info)))
      (info "bg20 is dead"))))

(defn exec-crawl [tlst]
  (let [q tlst]
    (start-worker q crawl-thread *thread-num*)))

(defn crawl-board [board]
  (when-let [tlst (get-thread-list (:title board) (:url board))]
    (infof "start bbs:%s" (:title board))
    (exec-crawl tlst)
    (info "fin")))

(defn start-crawl-all [store n] 
  (let [board-list (get-board-list)
        q (shuffle board-list)]
    (binding [*datastore* store]
      (debug *datastore*)
      (store/setup-store *datastore*)
      (debug "setup-store")
      (doseq [b board-list]
        (store/store-bbs *datastore* b))
      (start-worker q crawl-board n))))

(defn start-crawl [store bbs-parallel] 
  (binding [*datastore* store]
    (debug *datastore*)
    (store/setup-store *datastore*)
    (loop []
      (let [board-list (store/get-bbs *datastore*)
            q (shuffle board-list)]
        (doseq [b board-list]
          (store/store-bbs *datastore* b))
        (start-worker q crawl-board bbs-parallel)))))

(defn store-bbs-info [store bbs] 
  (binding [*datastore* store]
    (debug *datastore*)
    (store/setup-store *datastore*)
    (store/store-bbs *datastore* bbs)))

