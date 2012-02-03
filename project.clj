(defproject org-html-slides "0.0.1-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.4.0-alpha5"]
                 [org.clojure/clojurescript "0.0-971"]]
  :library-path "lib/jars"
  :source-path "src/clj"
  :extra-classpath-dirs ["lib/domina/src/cljs"
                         "lib/one/src/lib/cljs"])
