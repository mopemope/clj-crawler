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

(def ^:dynamic *thread-num* 2)

(def ^:dynamic *datastore*)

(defn- get-dat-url [board-url url]
  (let [[_ index] (re-find (re-matcher url-re url))]
    (format "%s%s%s/" bg20-url (subs board-url 7) index)))

(defn- get-board-list []
  (let [data (slurp *base-url* :encoding "MS932")]
    (debug "get-board-list")
    (parse-menu data)))

(defn- get-thread-list [board-nm board-url]
  (let [url (str board-url "subback.html")
        data (slurp url :encoding "MS932")]
    (debug  (format "get thread list '%s' '%s'" board-nm board-url))
    (parse-thread-list board-url data)))

(defn- get-thread-data [thread-info]
  (let [url (get-dat-url (:board-url thread-info) (:url thread-info))
        ^String data (slurp url :encoding "MS932")]
    (when (= (.indexOf data "ERROR = 5656") -1)
      (parse-thread thread-info data))))

(defn- crawl-thread [thread-info]
  (try
    (info (format "start %s" (:title thread-info)))
    (if-let [dats (get-thread-data thread-info)]
      (do
        (debug thread-info)
        (debug *datastore*)
        (let [old-cnt (store/store-thread *datastore* thread-info)]
          (if (>= old-cnt 0)
            (do
              (store/store-comments *datastore* (drop old-cnt dats))
              (info (format "stored %s" thread-info)))
            (info "進捗なし"))))
      (info "bg20 is dead"))
    (catch Exception e (error e))))

(defn exec-crawl [tlst]
  (let [q (atom tlst)]
    (debug "start-worker")
    (when-let [p (start-worker q crawl-thread *thread-num*)]
      @p)))

(defn crawl-board [board]
  (when-let [tlst (get-thread-list (:title board) (:url board))]
    (info (format "start %s" (:title board)))
    (exec-crawl tlst)
    (info "fin")))

(defn start-crawl [store n] 
  (let [board-list (get-board-list)
        q (atom (shuffle board-list))]
    (binding [*datastore* store]
      (debug *datastore*)
      (store/setup-store *datastore*)
      (when-let [p (start-worker q crawl-board n)]
        @p))))

; (def board (first (shuffle board-list)))
; (doseq [board (shuffle board-list)] 
  ; (do
    ; (crawl board)))
; (get-thread-list (:title board) (:url board))

; (database/store-thread thread-infos)

; (get-thread-data (first thread-infos))
; (clojure.string/replace "aa <br> 規模：" #"<br>" "\n")

