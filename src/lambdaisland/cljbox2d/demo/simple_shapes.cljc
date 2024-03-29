(ns lambdaisland.cljbox2d.demo.simple-shapes
  (:require [lambdaisland.cljbox2d :as b]
            [lambdaisland.cljbox2d.quil :as bq]
            [quil.core :as q :include-macros true]))

(declare world)

(def gravity 1)

(def walls
  [{:id       :ground
    :position [6 9.8]
    :fixtures [{:shape [:rect 12 0.4]}]}
   {:id       :left-wall
    :position [0.2 5]
    :fixtures [{:shape [:rect 0.4 10]}]}
   {:id       :right-wall
    :position [11.8 5]
    :fixtures [{:shape [:rect 0.4 10]}]}])

(defn random-body []
  {:position [(q/random 1 11) (q/random -2 7)]
   :type :dynamic
   :fixtures [{:shape
               (rand-nth
                [[:circle (q/random 0.2 0.6)]
                 [:rect (q/random 0.4 1.2) (q/random 0.4 1.2)]])
               :restitution 0.1
               :density 1
               :friction 3}]})

(defn setup []
  (alter-var-root #'world (constantly (b/world 0 gravity)))
  (q/stroke-weight 5)
  (-> world
      (b/populate walls)
      (b/populate (take 50 (repeatedly random-body)))))

(defn draw []
  (b/step-world world)
  (q/background 161 165 134)
  (bq/draw! world))

(defn -main []
  (q/defsketch box
    :host "app"
    :size [1200 1000]
    :setup setup
    :draw draw
    :frame-rate 60))

(comment
  (b/zoom! b/*camera* -10)
  (b/move-by! b/*camera* [-3 0]))
