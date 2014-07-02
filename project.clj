(defproject crawler "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 [clojure-soup "0.0.1"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [org.clojure/data.codec "0.1.0"]
                 [com.taoensso/timbre "3.2.1"]
                 [org.clojure/tools.cli "0.3.1"]
                 [clj-http "0.9.2"]
                 [korma "0.3.2"]
                 [clarango "0.4.2"]
                 [org.postgresql/postgresql "9.3-1101-jdbc4"]
                 ]
  :main crawler.core)
