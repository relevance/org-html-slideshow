(ns org-html-slides.main
  (:require [goog.debug.Logger :as Logger]
            [goog.debug.Console :as Console]
            [goog.array :as array]
            [goog.dom :as dom]
            [goog.dom.classes :as classes]
            [goog.style :as style]
            [goog.events :as events]
            [goog.events.KeyHandler :as KeyHandler]
            [goog.events.KeyCodes :as KeyCodes]
            [goog.Uri :as Uri]))

(def logger (Logger/getLogger "org_html_slides.main"))

(defn info [msg]
  (.info logger msg))

(defn add-image-classes []
  (doseq [img (array/toArray (dom/getElementsByTagNameAndClass "img"))]
    (let [p (. img parentNode)]
      (when (= "P" (. p nodeName))
        (classes/add p "image")))))

(add-image-classes)

(def current-slide (atom 0))

(def slideshow-mode (atom false))

(def loaded-slides (atom []))

(defn body-elem []
  (first (array/toArray (dom/getElementsByTagNameAndClass "body"))))

(def original-body-html
  (. (body-elem) innerHTML))

(defn stylesheet-link-elems [media-type]
  (vec (filter (fn [elem]
                 (and (= "stylesheet" (.. elem (getAttribute "rel") (toLowerCase)))
                      (. elem (getAttribute "media"))
                      (= media-type (.. elem (getAttribute "media") (toLowerCase)))))
               (array/toArray (dom/getElementsByTagNameAndClass "link")))))

(def original-screen-stylesheet-links
  (stylesheet-link-elems "screen"))

(def original-projection-stylesheet-links
  (stylesheet-link-elems "projection"))

(defn containing-slide-div [marker-elem]
  (some identity (for [n (range 8 0 -1)]
                   (dom/getAncestorByTagNameAndClass
                    marker-elem "div" (str "outline-" n)))))

(defn all-slide-markers []
  (array/toArray (dom/getElementsByTagNameAndClass "span" "slide")))

(defn all-slides []
  (vec (map containing-slide-div (all-slide-markers))))

(defn node-seq
  "Depth-first walk of the DOM as a lazy sequence, starting at elem."
  [elem]
  (when elem
   (lazy-seq
    (cons elem (node-seq
                (or (. elem firstChild)
                    (. elem nextSibling)
                    (when-let [parent (. elem parentNode)]
                      (. parent nextSibling))))))))

(defn first-slide-marker-after [elem]
  (first (filter (fn [elem]
                   (and (= "SPAN" (. elem nodeName))
                        (classes/has elem "slide")))
                 (node-seq elem))))

(defn replace-body [elem]
  (let [body (body-elem)]
    (set! (. body innerHTML) "")
    (.appendChild body elem)))

(defn show-current-slide []
  (let [slide (@loaded-slides @current-slide)]
    (replace-body slide)
    (let [uri (Uri/parse (. js/window location))]
      (. uri (setFragment (. slide id)))
      (set! (. js/window location) (str uri)))))

(defn show-original-html []
  (set! (. (body-elem) innerHTML) original-body-html))

(defn add-to-head [elem]
  (.appendChild (first (array/toArray (dom/getElementsByTagNameAndClass "head")))
                elem))

(defn remove-elem [elem]
  (.. elem parentNode (removeChild elem)))

(defn set-current-slide-by-uri-fragment []
  (let [uri (Uri/parse (. js/window location))]
    (when (. uri (hasFragment))
      (let [frag (. uri (getFragment))]
        (info (str "Fragment ID found: " frag))
        (when-let [marker (first-slide-marker-after (dom/getElement frag))]
          (let [slide-id (. (containing-slide-div marker) id)
                i (some identity (map-indexed (fn [i x] (when (= slide-id (. x id)) i)) @loaded-slides))]
            (info (str "Next slide ID found: " slide-id))
            (info (str "Corresponding slide number: " i))
            (reset! current-slide i)))))))

(defn enter-slideshow-mode []
  (info "Entering slideshow mode")
  (set-current-slide-by-uri-fragment)
  (show-current-slide)
  (doseq [elem (stylesheet-link-elems "screen")]
    (remove-elem elem))
  (doseq [elem original-projection-stylesheet-links]
    (.setAttribute elem "media" "screen")
    (add-to-head elem)))

(defn leave-slideshow-mode []
  (info "Leaving slideshow mode")
  (show-original-html)
  (doseq [elem (stylesheet-link-elems "screen")]
    (remove-elem elem))
  (doseq [elem original-screen-stylesheet-links]
    (add-to-head elem))
  (let [frag (. (Uri/parse (. js/window location)) (getFragment))]
    ;; Can't make goog.style.scrollIntoContainerView work,
    ;; don't know what the 'container' arg is supposed to be.
    (. (dom/getElement frag) (scrollIntoView))))

(defn show-next-slide []
  (when (< @current-slide (dec (count @loaded-slides)))
    (swap! current-slide inc)
    (show-current-slide)))

(defn show-prev-slide []
  (when (pos? @current-slide)
    (swap! current-slide dec)
    (show-current-slide)))

(defn toggle-mode []
  (if @slideshow-mode
    (leave-slideshow-mode)
    (enter-slideshow-mode))
  (swap! slideshow-mode not))

(defn handle-key [event]
  (let [code (. event keyCode)]
    (if @slideshow-mode
      (condp = code
        goog.events.KeyCodes.T (toggle-mode)

        goog.events.KeyCodes.SPACE (show-next-slide)
        goog.events.KeyCodes.ENTER (show-next-slide)        
        goog.events.KeyCodes.MAC_ENTER (show-next-slide)
        goog.events.KeyCodes.RIGHT (show-next-slide)
        goog.events.KeyCodes.DOWN (show-next-slide)
        goog.events.KeyCodes.PAGE_DOWN (show-next-slide)
        goog.events.KeyCodes.N (show-next-slide)

        goog.events.KeyCodes.LEFT (show-prev-slide)
        goog.events.KeyCodes.UP (show-prev-slide)
        goog.events.KeyCodes.PAGE_UP (show-prev-slide)
        goog.events.KeyCodes.P (show-prev-slide)
        nil)
      (condp = code
        goog.events.KeyCodes.T (toggle-mode)
        nil))))

(defn install-keyhandler []
  (events/listen (goog.events.KeyHandler. (dom/getDocument))
                 goog.events.KeyHandler.EventType.KEY
                 handle-key))

(defn main []
  (.setCapturing (goog.debug.Console.) true)
  (info "Application started")
  (reset! loaded-slides (all-slides))
  (info (str "Loaded " (count @loaded-slides) " slides"))
  (info (str "Found " (count original-screen-stylesheet-links) " screen stylesheets."))
  (info (str "Found " (count original-projection-stylesheet-links) " projection stylesheets."))
  (install-keyhandler)
  (info (str "Slide after #sec-2-2: "  (first-slide-marker-after (dom/getElement "sec-2-2")))))

(main)
