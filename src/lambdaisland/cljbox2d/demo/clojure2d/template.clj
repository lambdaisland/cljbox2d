(ns lambdaisland.cljbox2d.demo.clojure2d.template
  "Basic structure to start with"
  (:require [lambdaisland.cljbox2d :as b]
            [lambdaisland.cljbox2d.clojure2d :as bc2d]
            [clojure2d.core :as c2d])
  (:import [org.jbox2d.common Vec2]))

;; Create the world, we'll set gravity-y to 10 so things fall down
(def world (b/world 0 10))

;; Clojure2d setup function, this gets called once at the start
(defn setup [_canvas _window]
  (b/populate world [;; A static body that is basically just a straight line, so
                     ;; we have a "floor" for objects to rest on
                     {:fixtures [{:shape [:edge [-10 9.5] [20 9.5]]}]}
                     ;; A dynamic body, a 1x1 rectangular box
                     {:type :dynamic
                      :position [0 0]
                      :fixtures [{:shape [:rect 1 1]}]}])

  ;; Set the viewport, x, y, and scale
  (b/set-viewport! -2 -3 70))

;; This is the Clojur2d drawing function which gets called every "tick"
(defn draw [canvas _window _framecount _state]
  ;; Progress the physics animation
  (b/step-world world)

  (-> canvas
      ;; Clear the screen
      (c2d/set-background :white)
      ;; Set lines color
      (c2d/set-color :black)

      ;; Set stroke size
      (c2d/set-stroke 5)

      ;; "Draw" the world. This loops over all the things in the world and draws
      ;; them. By default it uses simple primitive shapes, but you can set
      ;; a :draw function on an entity to customize how it's drawn.
      (bc2d/draw! world)))

;; Launch Clojure2d window
(defn -main []
  (c2d/show-window {:canvas (c2d/canvas 1200 1000)
                    :draw-fn draw
                    :setup setup
                    :window-name "cljbox2d template"}))

;; Event handler, add new block after mouse pressed
(defmethod c2d/mouse-event ["cljbox2d template" :mouse-pressed] [event _state]
  (b/populate world [{:type :dynamic
                      :position (b/screen->world (Vec2. (c2d/mouse-x event) (c2d/mouse-y event)))
                      :fixtures [{:shape [:rect 1 1]}]}]))

(comment
  (-main))
