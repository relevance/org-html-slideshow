(ns build
  (:require [cljs.closure :as closure]
            [clojure.java.browse :as browse]
            [clojure.java.io :as io]))

(def options
  {:production
   {:output-dir "production"
    :output-to "production/org-html-slideshow.js"
    :optimizations :advanced}
   :development
   {:output-dir "out/development"
    :output-to "out/development/org-html-slideshow.js"
    :optimizations :whitespace
    :pretty-print true}})

(defn build [mode]
  {:pre [(contains? options mode)]}
  (closure/build "src/cljs" (options mode))
  (when (= mode :production)
    (doseq [f (filter #(and (.isFile %)
                            (.. % getName (endsWith ".css")))
                      (file-seq (io/file "src/css")))]
      (io/copy f (io/file "out/production" (.getName f))))))
