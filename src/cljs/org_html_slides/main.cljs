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

;;; GLOBAL STATE

(def stylesheet-urls
  "Atom containing map from mode (\"projection\" or \"screen\") to set
  of stylesheet URLs used only in that mode."
  (atom {}))

(def slides
  "Atom containing Vector of slide data"
  (atom nil))

(def document-body
  "Atom containing HTML of the original document body, after
  post-processing but before beginning a slideshow."
  (atom nil))

(def slideshow-mode? (atom false))


;;; UTILITIES

(defn info [& msgs]
  (.info (Logger/getLogger "org_html_slides.main") (apply pr-str msgs)))

(defn attr
  "Gets attribute from element and returns its value, lower-cased. If
  the element does not have the attribute returns nil."
  [elem attr]
  (when (. elem (hasAttribute attr))
    (.. elem (getAttribute attr) (toLowerCase))))

(defn dom-tags
  ([tag-name]
     (array/toArray (dom/getElementsByTagNameAndClass tag-name)))
  ([tag-name class-name]
     (array/toArray (dom/getElementsByTagNameAndClass tag-name class-name))))

(defn remove-elem
  "Remove a node from the DOM tree."
  [elem]
  (.. elem parentNode (removeChild elem)))

(defn add-to-head [elem]
  (.appendChild (first (dom-tags "head")) elem))

(defn body-elem []
  (first (dom-tags "body")))

(defn next-elem [elem]
  (or (. elem firstChild)
      (. elem nextSibling)
      (when-let [parent (. elem parentNode)]
        (. parent nextSibling))))

(defn location-fragment []
  (. (Uri/parse (. js/window location)) (getFragment)))

(defn set-location-fragment [fragment-id]
  (let [uri (Uri/parse (. js/window location))]
    (. uri (setFragment fragment-id))
    (set! (. js/window location) (str uri))))


;;; STYLESHEETS

(defn stylesheets [media-type]
  (set (map #(attr % "href")
            (filter (fn [elem]
                      (and (= "stylesheet" (attr elem "rel"))
                           (= media-type (attr elem "media"))))
                    (dom-tags "link")))))

(defn remove-stylesheets [urls]
  (doseq [elem (filter (fn [elem]
                         (and (= "stylesheet" (attr elem "rel"))
                              (contains? urls (attr elem "href"))))
                       (dom-tags "link"))]
    (remove-elem elem)))

(defn add-stylesheets [urls]
  (doseq [url urls]
    (add-to-head
     (doto (dom/createDom "link")
       (. (setAttribute "rel" "stylesheet"))
       (. (setAttribute "type" "text/css"))
       (. (setAttribute "href" url))))))


;;; SLIDES

(defn nearest-containing-div [elem]
  (if (= "DIV" (. elem nodeName))
    elem
    (recur (. elem parentNode))))

(def heading-tag-names (set (map #(str "H" %) (range 1 9))))

(defn nearest-inside-heading [elem]
  (if (contains? heading-tag-names (. elem nodeName))
    elem
    (recur (next-elem elem))))

(defn move-heading-tags-to-div-classes [classname]
  (doseq [marker (dom-tags "span" classname)]
    (classes/add (nearest-containing-div marker) classname)
    (remove-elem marker)))

(defn get-slides []
  (vec (map (fn [elem]
              {:id  (. (nearest-inside-heading elem) id)
               :html (. elem innerHTML)})
            (dom-tags "div" "slide"))))

(defn slide-from-id [id]
  (some (fn [slide] (when (= id (:id slide)) slide)) @slides))

(defn find-slide-after [id]
  (second (drop-while #(pos? (string/numerateCompare id (:id %)))
                      @slides)))

(defn current-slide []
  (let [fragment-id (location-fragment)]
    (or (slide-from-id fragment-id)
        (find-slide-after fragment-id)
        (first @slides))))

(defn show-slide [{:keys [id html]}]
  (set-location-fragment id)
  (set! (. (body-elem) innerHTML) html))


;;; GUI EVENTS

(defn enter-slideshow-mode []
  (info '(enter-slideshow-mode))
  (show-slide (current-slide))
  (remove-stylesheets (get @stylesheet-urls "screen"))
  (add-stylesheets (get @stylesheet-urls "projection")))

(defn leave-slideshow-mode []
  (info '(leave-slideshow-mode))
  (remove-stylesheets (get @stylesheet-urls "projection"))
  (add-stylesheets (get @stylesheet-urls "screen"))
  (set! (. (body-elem) innerHTML) @document-body)
  (. (dom/getElement (location-fragment)) (scrollIntoView)))

(defn toggle-mode []
  (info '(toggle-mode))
  (swap! slideshow-mode? not)
  (if @slideshow-mode?
    (enter-slideshow-mode)
    (leave-slideshow-mode)))

(defn show-next-slide []
  (let [current (current-slide)
        next (second (drop-while #(not= current %) @slides))]
    (when next (show-slide next))))

(defn show-prev-slide []
  (let [current (current-slide)
        prev (last (take-while #(not= current %) @slides))]
    (when prev (show-slide prev))))


;;; KEYBOARD

(def normal-keymap
  {goog.events.KeyCodes.T toggle-mode})

(def slideshow-keymap
  {goog.events.KeyCodes.T toggle-mode

   goog.events.KeyCodes.SPACE show-next-slide
   goog.events.KeyCodes.ENTER show-next-slide        
   goog.events.KeyCodes.MAC_ENTER show-next-slide
   goog.events.KeyCodes.RIGHT show-next-slide
   goog.events.KeyCodes.DOWN show-next-slide
   goog.events.KeyCodes.PAGE_DOWN show-next-slide
   goog.events.KeyCodes.N show-next-slide

   goog.events.KeyCodes.LEFT show-prev-slide
   goog.events.KeyCodes.UP show-prev-slide
   goog.events.KeyCodes.PAGE_UP show-prev-slide
   goog.events.KeyCodes.P show-prev-slide})

(defn handle-key [event]
  (let [code (. event keyCode)
        keymap (if @slideshow-mode? slideshow-keymap normal-keymap)
        command (get keymap code)]
    (when command
      (command)
      (. event (preventDefault))
      (. event (stopPropagation)))))

(defn install-keyhandler []
  (events/listen (goog.events.KeyHandler. (dom/getDocument))
                 goog.events.KeyHandler.EventType.KEY
                 handle-key))


;;; INITIAL SETUP

(defn init-stylesheets []
  (swap! stylesheet-urls assoc
         "projection" (stylesheets "projection")
         "screen" (stylesheets "screen")))

(defn add-image-classes []
  (doseq [img (dom-tags "img")]
    (let [p (. img parentNode)]
      (when (= "P" (. p nodeName))
        (classes/add p "image")))))


;;; MAIN

(defn main []
  (.setCapturing (goog.debug.Console.) true)
  (info "Application started")
  (info "Preparing document")
  (init-stylesheets)
  (add-image-classes)
  (move-heading-tags-to-div-classes "slide")
  (remove-stylesheets (get @stylesheet-urls "projection"))
  (info "Saving document and slides")
  (reset! document-body (. (body-elem) innerHTML))
  (reset! slides (get-slides))
  (info "Installing key handler")
  (install-keyhandler))

(main)
