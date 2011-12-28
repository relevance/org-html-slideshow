(ns org-html-slides.main
  (:require [goog.debug.Logger :as Logger]
            [goog.debug.Console :as Console]
            [goog.array :as array]
            [goog.dom :as dom]
            [goog.style :as style]
            [goog.events :as events]
            [goog.events.KeyHandler :as KeyHandler]
            [goog.events.KeyCodes :as KeyCodes]))

(def logger (Logger/getLogger "org_html_slides.main"))

(defn info [msg]
  (.info logger msg))

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

(defn replace-body [elem]
  (let [body (body-elem)]
    (set! (. body innerHTML) "")
    (.appendChild body elem)))

(defn show-current-slide []
  (replace-body (@loaded-slides @current-slide)))

(defn show-original-html []
  (set! (. (body-elem) innerHTML) original-body-html))

(defn add-to-head [elem]
  (.appendChild (first (array/toArray (dom/getElementsByTagNameAndClass "head")))
                elem))

(defn remove-elem [elem]
  (.. elem parentNode (removeChild elem)))

(defn enter-slideshow-mode []
  (info "Entering slideshow mode")
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
    (add-to-head elem)))

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
  (install-keyhandler))

(main)
