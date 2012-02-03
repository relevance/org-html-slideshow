(ns build
  (:require [cljs.closure :as closure]
            [clojure.java.browse :as browse]
            [clojure.java.io :as io]))

(def production-options
  {:output-dir "out/production"
   :output-to "out/production/org-html-slides.js"
   :optimizations :advanced})

(def development-options
  {:output-dir "out/development"
   :output-to "out/development/org-html-slides.js"
   :optimizations :whitespace
   :pretty-print true})

(defn build [mode]
  (closure/build "src/cljs"
                 (if (= mode :development)
                   development-options
                   default-options))
  (when (= mode :production)
    (doseq [f (filter #(and (.isFile %)
                            (.. % getName (endsWith ".css")))
                      (file-seq (io/file "src/css")))]
      (io/copy f (io/file "out/production" (.getName f))))))
