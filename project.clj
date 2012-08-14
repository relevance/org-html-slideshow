(defproject org-html-slides "0.0.1-SNAPSHOT"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.4.0-alpha5"]
                 [org.clojure/clojurescript "0.0-971"]]
  :library-path "lib/jars"
  :source-paths ["src/clj" "lib/domina/src/cljs" "lib/one/src/lib/cljs"])
