(ns lambdaisland.cljbox2d.demo.clojure2d.pyramid
  "Render a whole bunch of small squares that together form a Pyramid.
  Click with the mouse to simulate an explosion."
  (:require [lambdaisland.cljbox2d :as b]
            [lambdaisland.cljbox2d.math :as m]
            [lambdaisland.cljbox2d.clojure2d :as bc2d]
            [clojure2d.core :as c2d]))

(def world (b/world 0 10))

(defn setup [_ _]
  (-> world
      (b/populate [{:fixtures [{:shape [:edge [-10 9.5] [20 9.5]]}]}])
      (b/populate
       (for [i (range 20)
             j (range i 20)]
         {:type :dynamic
          :position [(+ 6 (* i 0.5625) (* j -0.25))
                     (* j 0.50)]
          :fixtures [{:shape [:rect 0.4 0.4]
                      :density 1
                      :friction 3}]})))

  (b/set-viewport! -2 -3 70))

(defn draw
  [canvas _ _ _]
  (b/step-world world)
  (-> canvas
      (c2d/set-background :white)
      (c2d/set-color :black)
      (c2d/set-stroke 5.0)
      (bc2d/draw! world)))

(defn apply-blast-impulse [body center apply-point power]
  (let [dir (m/v- apply-point center)
        distance (m/vec-length dir)]
    (when (not= 0 distance)
      (b/apply-impulse! body
                        (m/v* dir (* power (/ 1 distance) (/ 1 distance)))
                        apply-point
                        true))))

(defn explosion [center]
  (let [numrays 32
        blast-radius 10
        blast-power 6]
    (doseq [x (range numrays)
            :let [rad (* (/ x numrays) 2 Math/PI)
                  ray-end (m/v+ center (m/v* (b/vec2 (Math/sin rad) (Math/cos rad)) blast-radius))]
            {:keys [fixture point normal fraction]} (b/raycast-seq world center ray-end :all)]
      (apply-blast-impulse (b/body fixture) center point (/ blast-power numrays)))))

(defmethod c2d/mouse-event ["Pyramid" :mouse-pressed] [event _state]
  (explosion
   (b/screen->world (b/vec2 (.getX event) (.getY event)))))

(defn -main []
  (c2d/show-window {:canvas (c2d/canvas 1200 1000)
                    :draw-fn draw
                    :setup setup
                    :window-name "Pyramid"}))
