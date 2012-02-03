(ns build
  (:require [cljs.closure :as closure]
            [clojure.java.browse :as browse]))

(def default-options
  {:output-dir "out/production"
   :output-to "out/production/org-html-slides.js"
   :optimizations :advanced})

(def development-options
  {:output-dir "out/development"
   :output-to "out/development/org-html-slides.js"
   :optimizations :whitespace
   :pretty-print true})


(defn build [mode]
  (closure/build
   "src/cljs"
   (if (= mode :development)
     development-options
     default-options)))


(defn browse []
  (browse/browse-url ""))