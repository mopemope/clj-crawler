(ns crawler.crawler
    (import 
      (java.util.regex Pattern))
    (:require
      [crawler.datastore.datastore :as store])
    (:use 
      [clojure.tools.logging]
      [crawler.worker]
      [crawler.parser]))

(def ^:dynamic *base-url* "http://menu.2ch.net/bbsmenu.html")

(def ^:private bg20-url "http://bg20.2ch.net/test/r.so/")

(def ^:private url-re (Pattern/compile ".*/(\\d+)/.*" Pattern/DOTALL))

(def ^:dynamic *thread-num* 3)

(def ^:dynamic *datastore*)

(defn- get-dat-url [board-url url]
  (let [[_ index] (re-find (re-matcher url-re url))]
    (format "%s%s%s/" bg20-url (subs board-url 7) index)))

(defn get-board-list []
  (let [data (slurp *base-url* :encoding "MS932")]
    (debug "get-board-list")
    (parse-menu data)))

(defn- get-thread-list [board-nm board-url]
  (let [url (str board-url "subback.html")
        data (slurp url :encoding "MS932")]
    (debug  (format "get thread list title:'%s' url:'%s'" board-nm board-url))
    (parse-thread-list board-url data)))

(defn- get-thread-data [thread-info]
  (let [url (get-dat-url (:board-url thread-info) (:url thread-info))
        ^String data (slurp url :encoding "MS932")]
    (debug (format "load dat from url:%s" url))
    (if (= (.indexOf data "ERROR = 5656") -1)
      (parse-thread thread-info data)
      (info (format "error load url:%s" url)))))

(defn- crawl-thread [thread-info]
  (do
    (info (format "start %s" (:title thread-info)))
    (if-let [comment-info (get-thread-data thread-info)]
      (store/store-thread-comments *datastore* thread-info comment-info)
      (info "bg20 is dead"))))

(defn exec-crawl [tlst]
  (let [q (atom tlst)]
    (when-let [p (start-worker q crawl-thread *thread-num*)]
      @p)))

(defn crawl-board [board]
  (when-let [tlst (get-thread-list (:title board) (:url board))]
    (info (format "start bbs:%s" (:title board)))
    (exec-crawl tlst)
    (info "fin")))

(defn start-crawl-all [store n] 
  (let [board-list (get-board-list)
        q (atom (shuffle board-list))]
    (binding [*datastore* store]
      (debug *datastore*)
      (store/setup-store *datastore*)
      (when-let [p (start-worker q crawl-board n)]
        @p))))

(defn start-crawl [store n] 
  (binding [*datastore* store]
    (debug *datastore*)
    (store/setup-store *datastore*)
    (loop []
      (let [board-list (store/get-bbs *datastore*)
            q (atom (shuffle board-list))]
        (when-let [p (start-worker q crawl-board n)]
          @p
          (info "end all wait...")
          (Thread/sleep 5000))))))

(defn store-bbs-info [store bbs] 
  (binding [*datastore* store]
    (debug *datastore*)
    (store/setup-store *datastore*)
    (store/store-bbs *datastore* bbs)))

