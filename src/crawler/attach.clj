(ns crawler.attach
  (import 
   (java.util.regex Pattern)
   (org.jsoup.nodes Element))
  (:require
   [clojure.core.async :as async :refer [<! >! >!! <!! timeout chan alts! alts!! go sliding-buffer]]
   [clojure.data.codec.base64 :as b64]
   [taoensso.timbre :as timbre]
   [clj-http.client :as client]   
   ))

(timbre/refer-timbre)

(def ^:private cmt-re (Pattern/compile "([\\w-:/.]+\\.jpg)|([\\w:/.]+\\.jpeg)|([\\w:/.]*\\.png)|([\\w:/.]*imepita.jp/[\\w./]+)|([\\w:/.]*imepic.jp/[\\w./]+)|([\\w:/.]*i.imgur.com/[\\w./]+)" Pattern/DOTALL))

(defn- base64-string [data]
  (String. (b64/encode data) "UTF-8"))

(defn- download [url]
  (try
    (let [res (client/get url {:as :byte-array})
          status (:status res)
          ^String ct (:Content-Type (:headers res))
          ^String length (:Content-Length (:headers res))]
      (when (and (= status 200) (not (.startsWith ct "text/html")) (not= length "503"))
        (infof "start download from %s" url)
        [ct (base64-string (:body res))]))
    (catch Exception e
      (error e ))))

(defn- complement-url [^String url]
  (cond
   (.startsWith url "http://") url
   (.startsWith url "ttp://") (str "h" url)
   :else (str "http://" url)))

(defn search-attachment [data store-fn]
  (doseq [line (clojure.string/split (:comment data) #"\n") ]
    (let [[m] (re-find (re-matcher cmt-re line))]
      (when m
        (let [url (complement-url (clojure.string/trim m))]
          (when-let [[type bdata] (download url)]
            (store-fn data type bdata)))))))

