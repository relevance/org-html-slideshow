(ns org-html-slides.main
  (:require [goog.debug.Logger :as Logger]
            [goog.debug.Console :as Console]
            [goog.array :as array]
            [goog.dom :as dom]
            [goog.dom.classes :as classes]
            [goog.string :as string]
            [goog.style :as style]
            [goog.events :as events]
            [goog.events.EventType :as EventType]
            [goog.events.KeyHandler :as KeyHandler]
            [goog.events.KeyCodes :as KeyCodes]
            [goog.string.format :as googstringformat]
            [goog.Timer :as Timer]
            [goog.Uri :as Uri]
            [goog.window :as window]
            [one.logging :as logging]
            [one.dispatch :as dispatch]
            [domina :as d]))

;;; GLOBAL STATE

(def stylesheet-urls
  "Atom containing map from mode (\"projection\" or \"screen\") to set
  of stylesheet URLs used only in that mode."
  (atom {}))

(def slides
  "Atom containing Vector of slide data"
  (atom nil))

(def slideshow-mode? (atom false))

(def presenter-window (atom nil))

(def presenter-start-time (atom nil))


;;; UTILITIES

(defn info [& msgs]
  (logging/info (logging/get-logger "org_html_slides.main")
                (apply pr-str msgs)))

(defn dom-tags
  ([tag-name]
     (array/toArray (dom/getElementsByTagNameAndClass tag-name)))
  ([tag-name class-name]
     (array/toArray (dom/getElementsByTagNameAndClass tag-name class-name)))
  ([tag-name class-name inside-elem]
     (array/toArray (dom/getElementsByTagNameAndClass tag-name class-name inside-elem))))

(defn remove-elem
  "Remove a node from the DOM tree."
  [elem]
  (.. elem -parentNode (removeChild elem)))

(defn add-to-head
  ([elem] (add-to-head elem nil))
  ([elem parent]
     (.appendChild (first (dom-tags "head" nil parent)) elem)))

(defn body-elem []
  (first (dom-tags "body")))

(defn next-elem [elem]
  (or (. elem -firstChild)
      (. elem -nextSibling)
      (when-let [parent (. elem -parentNode)]
        (. parent -nextSibling))))

(defn location-fragment []
  (. (Uri/parse (. js/window -location)) (getFragment)))

(defn set-location-fragment [fragment-id]
  (let [uri (Uri/parse (. js/window -location))]
    (. uri (setFragment fragment-id))
    (set! (. js/window -location) (str uri))))

(defn fire-handler [event-id]
  (fn [goog-event]
    (when goog-event   ; goog.Timer sends nil event
     (. goog-event (preventDefault))
     (. goog-event (stopPropagation)))
    (dispatch/fire event-id goog-event)))

(defn show! [content]
  (when content (style/showElement (d/single-node content) true)))

(defn hide! [content]
  (when content (style/showElement (d/single-node content) false)))


;;;

(defn stylesheets [media-type]
  (set (map #(d/attr % "href")
            (filter (fn [elem]
                      (and (= "stylesheet" (d/attr elem "rel"))
                           (= media-type (d/attr elem "media"))))
                    (dom-tags "link")))))

(defn remove-stylesheets [urls]
  (doseq [elem (filter (fn [elem]
                         (and (= "stylesheet" (d/attr elem "rel"))
                              (contains? urls (d/attr elem "href"))))
                       (dom-tags "link"))]
    (remove-elem elem)))

(defn add-stylesheets
  ([urls] (add-stylesheets urls nil))
  ([urls parent]
     (doseq [url urls]
       (add-to-head
        (doto (dom/createDom "link")
          (. (setAttribute "rel" "stylesheet"))
          (. (setAttribute "type" "text/css"))
          (. (setAttribute "href" url)))
        parent))))


;;; FOLD-OUT CONTENT

(defn get-folds []
  (vec (map (fn [elem]
              {:head-elem (.. elem -parentNode -parentNode)
               :body-elem (first (dom-tags "div" nil (nearest-containing-div elem)))})
            (dom-tags "span" "fold"))))

(def show-hide-html
  " <a href=\"#\" class=\"show-hide\"><span>show/hide</span></a>")

(defn toggle-visibility [head body]
  (if (style/isElementShown body)
    (do (style/showElement body false)
        (classes/remove head "unfolded")
        (classes/add head "folded"))
    (do (style/showElement body true)
        (classes/remove head "folded")
        (classes/add head "unfolded"))))

(defn handle-show-hide [event]
  (. event (preventDefault))
  (let [head-elem (. event -currentTarget)
        body-elem (first (dom-tags "div" nil (nearest-containing-div head-elem)))]
    (toggle-visibility head-elem body-elem)))

(defn install-folds []
  (doseq [{:keys [head-elem body-elem]} (get-folds)]
    (toggle-visibility head-elem body-elem)
    (let [a (dom/htmlToDocumentFragment show-hide-html)]
      (. head-elem (appendChild a))
      (let [a (dom-tags "a" "show-hide" head-elem)]
        (events/listen head-elem goog.events.EventType.CLICK
                       handle-show-hide)))))

;;; CONTROL PANEL

(def control-html
  "<div id=\"c-panel\">
<a id=\"c-toggle\" href=\"#\">
  <span class=\"label\">Toggle slide-show mode</span>
  <span class=\"key\">T</span>
</a>
<a id=\"c-first\" href=\"#\">
  <span class=\"label\">First slide</span>
  <span class=\"key\">Home</span>
</a>
<a id=\"c-prev\" href=\"#\">
  <span class=\"label\">Previous slide</span>
  <span class=\"key\">P</span>
</a>
<a id=\"c-next\" href=\"#\">
  <span class=\"label\">Next slide</span>
  <span class=\"key\">N</span>
</a>
<a id=\"c-last\" href=\"#\">
  <span class=\"label\">Last slide</span>
  <span class=\"key\">End</span>
</a>
<a id=\"c-presenter-window\" href=\"#\">
  <span class=\"label\">Open presenter preview</span>
</a>
</div>")

(defn show-control-panel []
  (style/setStyle (dom/getElement "c-panel") "opacity" 0.75))

(defn hide-control-panel []
  (style/setStyle (dom/getElement "c-panel") "opacity" 0.0))

(defn install-control-panel []
  (. (body-elem) (appendChild (dom/htmlToDocumentFragment control-html)))
  (let [panel (dom/getElement "c-panel")]
    (dispatch/fire :show-control-panel)
    (Timer/callOnce (fire-handler :hide-control-panel) 3000)
    (events/listen panel goog.events.EventType.MOUSEOVER
                   (fire-handler :show-control-panel))
    (events/listen panel goog.events.EventType.MOUSEOUT
                   (fire-handler :hide-control-panel))
    (events/listen (dom/getElement "c-toggle")
                   goog.events.EventType.CLICK
                   (fire-handler :toggle-mode))
    (events/listen (dom/getElement "c-first")
                   goog.events.EventType.CLICK
                   (fire-handler :show-first-slide))
    (events/listen (dom/getElement "c-prev")
                   goog.events.EventType.CLICK
                   (fire-handler :show-prev-slide))
    (events/listen (dom/getElement "c-next")
                   goog.events.EventType.CLICK
                   (fire-handler :show-next-slide))
    (events/listen (dom/getElement "c-last")
                   goog.events.EventType.CLICK
                   (fire-handler :show-last-slide))
    (events/listen (dom/getElement "c-presenter-window")
                   goog.events.EventType.CLICK
                   (fire-handler :show-presenter-window))))


;;; SLIDES

(def current-slide-div-html
  "<div id=\"current-slide\"></div>")

(defn nearest-containing-div [elem]
  (if (= "DIV" (. elem -nodeName))
    elem
    (recur (. elem -parentNode))))

(def heading-tag-names (set (map #(str "H" %) (range 1 9))))

(defn nearest-inside-heading [elem]
  (if (contains? heading-tag-names (. elem -nodeName))
    elem
    (recur (next-elem elem))))

(defn copy-heading-tags-to-div-classes []
  (doseq [tags (dom-tags "span" "tag")]
    (let [div (nearest-containing-div tags)]
      (doseq [tag (dom-tags "span" nil tags)]
        (classes/add div (classes/get tag))))))

(defn remove-nested-sections [slide-div-elem]
  (let [div (. slide-div-elem (cloneNode true))]
    (doseq [elem (dom-tags "div" nil div)]
      (when (some #(classes/has elem (str "outline-" %)) (range 1 9))
        (remove-elem elem)))
    div))

(defn get-slides []
  (vec (map (fn [elem]
              {:id  (. (nearest-inside-heading elem) -id)
               :html (dom/getOuterHtml (remove-nested-sections elem))})
            (dom-tags "div" "slide"))))

(defn slide-from-id [id]
  (some (fn [slide] (when (= id (:id slide)) slide)) @slides))

(defn find-slide-after [id]
  (second (drop-while #(pos? (string/numerateCompare id (:id %)))
                      @slides)))

(defn current-slide []
  (let [fragment-id (location-fragment)]
    (or (slide-from-id fragment-id)
        (and (seq fragment-id) (find-slide-after fragment-id))
        (first @slides))))

(defn next-slide []
  (find-slide-after (:id (current-slide))))

(defn show-slide [{:keys [id html]}]
  (set-location-fragment id)
  (set! (. (dom/getElement "current-slide") -innerHTML) html)
  (show-presenter-slides))


;;; GUI EVENTS

(defn enter-slideshow-mode []
  (info '(enter-slideshow-mode))
  (hide! (d/by-id "preamble"))
  (hide! (d/by-id "content"))
  (hide! (d/by-id "postamble"))
  (remove-stylesheets (get @stylesheet-urls "screen"))
  (add-stylesheets (get @stylesheet-urls "projection"))
  (show! (d/by-id "current-slide"))
  (show-slide (current-slide)))

(defn leave-slideshow-mode []
  (info '(leave-slideshow-mode))
  (hide! (d/by-id "current-slide"))
  (remove-stylesheets (get @stylesheet-urls "projection"))
  (add-stylesheets (get @stylesheet-urls "screen"))
  (show! (d/by-id "preamble"))
  (show! (d/by-id "content"))
  (show! (d/by-id "postamble"))
  (. (dom/getElement (location-fragment)) (scrollIntoView)))

(defn change-mode []
  (if @slideshow-mode?
    (enter-slideshow-mode)
    (leave-slideshow-mode)))

(defn toggle-mode []
  (info '(toggle-mode))
  (swap! slideshow-mode? not))

(add-watch slideshow-mode? :change-mode
           (fn [k r o n]
             (dispatch/fire :change-mode)))

(defn show-next-slide []
  (let [current (current-slide)
        next (second (drop-while #(not= current %) @slides))]
    (when next (show-slide next))
    (swap! presenter-start-time (fn [t]
                                  (if (nil? t)
                                    (.getTime (js/Date.))
                                    t)))))

(defn show-prev-slide []
  (let [current (current-slide)
        prev (last (take-while #(not= current %) @slides))]
    (when prev (show-slide prev))))

(defn show-first-slide []
  (show-slide (first @slides)))

(defn show-last-slide []
  (show-slide (last @slides)))

(defn go-to-top []
  (set-location-fragment "top")
  (. window (scrollTo 0 0)))


;;; KEYBOARD

(def normal-keymap
  {goog.events.KeyCodes.T :toggle-mode
   goog.events.KeyCodes.HOME :go-to-top})

(def slideshow-keymap
  {goog.events.KeyCodes.T :toggle-mode

   goog.events.KeyCodes.HOME :show-first-slide
   goog.events.KeyCodes.END :show-last-slide

   goog.events.KeyCodes.SPACE :show-next-slide
   goog.events.KeyCodes.ENTER :show-next-slide        
   goog.events.KeyCodes.MAC_ENTER :show-next-slide
   goog.events.KeyCodes.RIGHT :show-next-slide
   goog.events.KeyCodes.DOWN :show-next-slide
   goog.events.KeyCodes.PAGE_DOWN :show-next-slide
   goog.events.KeyCodes.N :show-next-slide

   goog.events.KeyCodes.LEFT :show-prev-slide
   goog.events.KeyCodes.UP :show-prev-slide
   goog.events.KeyCodes.PAGE_UP :show-prev-slide
   goog.events.KeyCodes.P :show-prev-slide})

(defn handle-key [event]
  (let [code (. event -keyCode)
        keymap (if @slideshow-mode? slideshow-keymap normal-keymap)
        event-id (get keymap code)]
    (when event-id
      (dispatch/fire event-id)
      (. event (preventDefault))
      (. event (stopPropagation)))))

(defn install-keyhandler []
  (events/listen (goog.events.KeyHandler. (dom/getDocument))
                 goog.events.KeyHandler.EventType.KEY
                 handle-key))


;;; PRESENTER WINDOW

(def presenter-display-html
  "
<html>
  <head>
  </head>
  <body class=\"presenter-display\">
    <div id=\"presenter-slide-preview\">
      <div id=\"presenter-current-slide-container\">
        <h2 class=\"presenter-label\">Current Slide</h2>
        <div id=\"presenter-current-slide\">
        </div>
      </div>
      <div id=\"presenter-next-slide-container\">
        <h2 class=\"presenter-label\">Next Slide</h2>
        <div id=\"presenter-next-slide\">
        </div>
      </div>
     </div>
     <div id=\"presenter-times\" class=\"presenter-label\">
       <div id=\"presenter-elapsed-time\"><h2>0:00:00</h2></div>
       <div id=\"presenter-clock-time\"><h2></h2></div>
     </div>
  </body>
</html>
")

(defn get-presenter-window []
  (when @presenter-window
    (if (. @presenter-window -closed)
      (reset! presenter-window nil)
      @presenter-window)))

(defn update-presenter-clock-time [win]
  (let [elem (.. win -document
                 (getElementById "presenter-clock-time"))
        now (js/Date.)]
    (set! (. elem -innerHTML)
          (goog.string.format
           "<h2>%d:%02d:%02d %s</h2>"
           (rem (. now (getHours)) 12)
           (. now (getMinutes))
           (.. now (getSeconds))
           (if (< 12 (. now (getHours)))
             "PM" "AM")))))

(defn update-presenter-elapsed-time [win]
  (when @presenter-start-time
   (let [elem (.. win -document
                  (getElementById "presenter-elapsed-time"))
         elapsed (- (.getTime (js/Date.)) @presenter-start-time)
         secs (mod (/ elapsed 1000) 60)
         mins (mod (/ elapsed (* 60 1000)) 60)
         hours (/ elapsed (* 60 60 1000))]
     (set! (. elem -innerHTML)
           (goog.string.format
            "<h2>%d:%02d:%02d</h2>"
            hours mins secs)))))

(defn update-presenter-clock []
  (when-let [win (get-presenter-window)]
    (update-presenter-clock-time win)
    (update-presenter-elapsed-time win)
    (. js/window (setTimeout update-presenter-clock 1000))))

(defn show-presenter-slides []
  (when-let [win (get-presenter-window)]
    (let [div (.. win -document
                  (getElementById "presenter-current-slide"))]
      (set! (. div -innerHTML) (:html (current-slide))))
    (let [div (.. win -document
                  (getElementById "presenter-next-slide"))]
      (set! (. div -innerHTML) (:html (next-slide))))))

(defn show-presenter-window []
  (if-let [win (get-presenter-window)]
    (. win (focus))
    (do (reset! presenter-window
             (window/open "" (. {:target "PRESENTERDISPLAY"
                                 :toolbar false
                                 :location false
                                 :statusbar false
                                 :menubar false}
                                -strobj)))
        (let [doc (. @presenter-window -document)]
          (. doc (write presenter-display-html))
          (add-stylesheets (get @stylesheet-urls "common") doc)
          (add-stylesheets (get @stylesheet-urls "projection") doc)
          (add-stylesheets (get @stylesheet-urls "presenter") doc))
        (show-presenter-slides)
        (update-presenter-clock))))


;;; EVENTS

(defn install-event-handlers []
  (dispatch/react-to #{:show-next-slide} (fn [id _] (show-next-slide)))
  (dispatch/react-to #{:show-prev-slide} (fn [id _] (show-prev-slide)))
  (dispatch/react-to #{:show-first-slide} (fn [id _] (show-first-slide)))
  (dispatch/react-to #{:show-last-slide} (fn [id _] (show-last-slide)))
  (dispatch/react-to #{:toggle-mode} (fn [id _] (toggle-mode)))
  (dispatch/react-to #{:go-to-top} (fn [id _] (go-to-top)))
  (dispatch/react-to #{:show-control-panel} (fn [id _] (show-control-panel)))
  (dispatch/react-to #{:hide-control-panel} (fn [id _] (hide-control-panel)))
  (dispatch/react-to #{:change-mode} (fn [id _] (change-mode)))
  (dispatch/react-to #{:show-presenter-window} (fn [id _] (show-presenter-window))))

;;; INITIAL SETUP

(defn init-stylesheets []
  (swap! stylesheet-urls assoc
         "projection" (stylesheets "projection")
         "screen" (stylesheets "screen")
         "common" (stylesheets nil)
         "presenter" (stylesheets "presenter")))

(defn add-image-classes []
  (doseq [img (dom-tags "img")]
    (let [p (. img -parentNode)]
      (when (= "P" (. p -nodeName))
        (classes/add p "image")))))

(defn add-outline-text-class []
  (doseq [i (range 1 9)]
    (doseq [elem (dom-tags "div" (str "outline-text-" i))]
      (classes/add elem "outline-text"))))


;;; MAIN

(defn main []
  (.setCapturing (goog.debug.Console.) true)
  (info "Application started")
  (info "Preparing document")
  (init-stylesheets)
  (remove-stylesheets (get @stylesheet-urls "projection"))
  (remove-stylesheets (get @stylesheet-urls "presenter"))
  (add-image-classes)
  (copy-heading-tags-to-div-classes)
  (add-outline-text-class)
  (install-folds)
  (. (body-elem)
     (appendChild (dom/htmlToDocumentFragment current-slide-div-html)))
  (hide! (d/by-id "current-slide"))
  (reset! slides (get-slides))
  (info '(count slides) (count @slides))
  (info "Installing key handler")
  (install-event-handlers)
  (install-control-panel)
  (install-keyhandler))

(main)
