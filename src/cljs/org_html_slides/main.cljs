(ns org-html-slides.main
  (:require [goog.debug.Logger :as Logger]
            [goog.debug.Console :as Console]
            [goog.array :as array]
            [goog.dom :as dom]
            [goog.style :as style]
            [goog.events :as events]
            [goog.events.KeyHandler :as KeyHandler]
            [goog.events.KeyCodes :as KeyCodes]))

(def logger (Logger/getLogger "training.main"))

(defn info [msg]
  (.info logger msg))

(def current-slide (atom 0))

(def slideshow-mode (atom false))

(def loaded-slides (atom []))

(def original-container-html (. (dom/getElement "container") innerHTML))

(defn containing-slide-div [marker-elem]
  (some identity (for [n (range 8 0 -1)]
                   (dom/getAncestorByTagNameAndClass
                    marker-elem "div" (str "outline-" n)))))

(defn all-slide-markers []
  (array/toArray (dom/getElementsByTagNameAndClass "span" "slide")))

(defn all-slides []
  (vec (map containing-slide-div (all-slide-markers))))

(defn show-in-container [elem]
  (let [container (dom/getElement "container")]
    (set! (. container innerHTML) "")
    (.appendChild container elem)))

(defn show-current-slide []
  (show-in-container (@loaded-slides @current-slide)))

(defn show-original-html []
  (set! (. (dom/getElement "container") innerHTML) original-container-html))

(defn slides-stylesheet-link-elem []
  (some (fn [elem]
          (when (= (.getAttribute elem "href") "css/slides.css") elem))
        (array/toArray (dom/getElementsByTagNameAndClass "link"))))

(defn enter-slideshow-mode []
  (info "Entering slideshow mode")
  (show-current-slide)
  (.setAttribute (slides-stylesheet-link-elem) "media" "screen"))

(defn leave-slideshow-mode []
  (info "Leaving slideshow mode")
  (show-original-html)
  (.setAttribute (slides-stylesheet-link-elem) "media" "projection"))

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
  (install-keyhandler))

(main)
