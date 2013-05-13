(defproject crawler "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.5.1"]
                 [clojure-soup "0.0.1"]
                 [org.clojure/tools.logging "0.2.6"]
                 [korma "0.3.0-RC5"]
                 [ch.qos.logback/logback-classic "1.0.9"]
                 [org.postgresql/postgresql "9.2-1002-jdbc4"]
                 ]
  :main crawler.core)
