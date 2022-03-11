(ns lambdaisland.cljbox2d.demo.template
  "Basic structure to start with"
  (:require [lambdaisland.cljbox2d :as b]
            [lambdaisland.cljbox2d.quil :as bq]
            [quil.core :as q :include-macros true]))

;; Create the world, we'll set gravity-y to 10 so things fall down
(def world (b/world 0 10))

;; Quil setup function, this gets called once at the start
(defn setup []
  (b/populate world [;; A static body that is basically just a straight line, so
                     ;; we have a "floor" for objects to rest on
                     {:fixtures [{:shape [:edge [-10 9.5] [20 9.5]]}]}
                     ;; A dynamic body, a 1x1 rectangular box
                     {:type :dynamic
                      :position [0 0]
                      :fixtures [{:shape [:rect 1 1]}]}])

  ;; Set the viewport, x, y, and scale
  (b/set-viewport! -2 -3 70))

;; This is the Quil drawing function which gets called every "tick"
(defn draw []
  ;; Progress the physics animation
  (b/step-world world)

  ;; Clear the screen
  (q/background 255)

  ;; Set drawing parameters
  (q/stroke-weight 5)

  ;; "Draw" the world. This loops over all the things in the world and draws
  ;; them. By default it uses simple Quil primitive shapes, but you can set
  ;; a :draw function on an entity to customize how it's drawn.
  (bq/draw! world))

;; Launch Quil
(defn -main []
  (q/defsketch my-sketch
    :host "app"
    :size [1200 1000]
    :draw draw
    :setup setup
    :frame-rate 60))

(comment
  (-main))
