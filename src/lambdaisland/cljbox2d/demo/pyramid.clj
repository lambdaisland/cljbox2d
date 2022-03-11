(ns lambdaisland.cljbox2d.demo.pyramid
  (:require [lambdaisland.cljbox2d :as b]
            [lambdaisland.cljbox2d.quil :as bq]
            [quil.core :as q :include-macros true]))

(def world (b/world 0 10))

(defn setup []
  (-> world
      (b/populate [{:fixtures [{:shape [:edge [-10 9.5] [20 9.5]]}]}])
      (b/populate
       (for [i (range 20)
             j (range i 20)]
         {:type :dynamic
          :position [(+ 6 (* i 0.5625) (* j -0.25))
                     (+ -3 (* j 0.6))]
          :fixtures [{:shape [:rect 0.4 0.4]}]})))

  (b/set-viewport! -2 -3 70))

(defn draw []
  (q/stroke-weight 5)
  (b/step-world world)
  (q/background 255)
  (bq/draw! world))

(defn -main []
  (q/defsketch pyramid
    :host "app"
    :size [1200 1000]
    :draw draw
    :setup setup
    :frame-rate 60))
