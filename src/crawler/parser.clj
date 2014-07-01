(ns crawler.parser
  (import 
   (java.util.regex Pattern)
   (org.jsoup.nodes Element))
  (:require
   [taoensso.timbre :as timbre])
  (:use 
   [jsoup.soup]))

(timbre/refer-timbre)

(def ^:private bg20-url "http://bg20.2ch.net/test/r.so/")

(def ^:dynamic *ignore-menus* ["guide/" "bbsmenu" "php" ".tv/" ".net/" ".jp/"])

(def ^:private title-re (Pattern/compile "\\d:\\s+(.*)\\s*\\((\\d+)\\)" Pattern/DOTALL))

(defn- parse-int [s]
  (Integer. ^String (re-find  #"\d+" s)))

(defn- get-thread-info [title]
  (let [[_ title res-count] (re-find (re-matcher title-re title))]
    {:title title :res-count (parse-int res-count)}))

(defn- format-url [root ^String href]
  (str root (subs href 0 (- (.length href) 3))))

(defn- ends-with [^String href words]
  (some #(.endsWith href %) words))

(defn- menu? [href menu]
  (when (and 
          (not (empty? menu))
          (not (ends-with href *ignore-menus*)))
    {:url href :title menu}))

(defn- menu-filter [elements]
  (loop [elements elements result []]
    (if (nil? elements)
      result
      (let [^Element e (first elements)
            href (.attr e "href")
            menu (.text e)]
        (if-let [val (menu? href menu)]
          (recur (next elements) (conj result val))
          (recur (next elements) result))))))

(defn parse-menu [data]
  ($ (parse data)
     "a[href^=http]"
     (menu-filter)))

(defn parse-thread-list [board-url data]
  (let [base-url (first ($ (parse data) 
                           "base" 
                           (attr "href")))]
    (when base-url
      ($ (parse data) 
           "a[href$=50]" 
           (map #(assoc (get-thread-info (.text ^Element %)) 
                        :board-url board-url 
                        :url (format-url base-url (.attr ^Element % "href"))))))))

(defn- create-dat-info [thread-info]
  (let [board-url (:board-url thread-info)
        title (:title thread-info)
        url (:url thread-info)] 
    (fn [idx line] 
      (let [[hndl mailto date cmt] (clojure.string/split line #"<>")]
        {
        :board-url board-url
        :title title 
        :url url 
        :no (inc idx) 
        :handle hndl 
        :mailto mailto 
        :date date 
        :comment (clojure.string/replace cmt #"<br>" "\n")}))))

(defn parse-thread [thread-info data]
  (let [lines (clojure.string/split data #"\r\n|\n")]
    (map-indexed (create-dat-info thread-info) lines)))

