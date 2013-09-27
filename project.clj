(defproject org-html-slides "0.0.1-SNAPSHOT"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1896"]]
  :plugins [[lein-cljsbuild "0.3.3"]]
  :library-path "lib/jars"
  :source-paths ["lib/domina/src/cljs" "lib/one/src/lib/cljs"]
  :cljsbuild {:builds
              {:development
               {:source-paths ["src/cljs"]
                :compiler {:output-to "out/development/org-html-slideshow.js"
                           :output-dir "out/development"
                           :optimizations :whitespace
                           :pretty-print true}}
               :production
               {:source-paths ["src/cljs"]
                :compiler {:output-to "production/org-html-slideshow.js"
                           :output-dir "production"
                           :optimizations :advanced
                           :pretty-print false}}}})
