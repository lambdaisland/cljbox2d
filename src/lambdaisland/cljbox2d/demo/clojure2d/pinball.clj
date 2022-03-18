(ns lambdaisland.cljbox2d.demo.clojure2d.pinball
  (:require [lambdaisland.cljbox2d :as b]
            [lambdaisland.cljbox2d.math :as m]
            [lambdaisland.cljbox2d.clojure2d :as bc2d]
            [clojure2d.core :as c2d]))

(def state (atom {}))

(defn world [] (:world @state))

(defn setup [canvas _]
  (let [{:keys [world]} @state])
  )

(defn draw [canvas _ _ _]
  (let [{:keys [world]} @state]
    (b/step-world world)
    (-> canvas
        (c2d/set-background :white)
        (c2d/set-color :black)
        (c2d/set-stroke 5.0)
        (bc2d/draw! world))))

(defmethod c2d/mouse-event ["Pinball" :mouse-pressed] [event _state]
  (prn (b/vec2 (.getX event) (.getY event)) (b/screen->world (b/vec2 (.getX event) (.getY event)))))


(defn -main []
  (let [canvas (c2d/canvas 600 1000)]
    (reset! state {:world (b/world 0 10)
                   :canvas canvas})
    (let [window (c2d/show-window {:canvas canvas
                                   :draw-fn draw
                                   :setup setup
                                   :window-name "Pinball"})]
      (swap! state assoc :window window))))


(comment
  (-main)

  (swap! state assoc :world (b/world 0 10))

  (b/populate
   (world)
   [{:id :case
     :fixtures [{:shape [:edge [0.3 0.3] [5.7 0.3]]}
                {:shape [:edge [5.7 0.3] [5.7 9.7]]}
                #_{:shape [:edge [4.7 9.7] [0.3 9.7]]}
                {:shape [:edge [0.3 9.7] [0.3 0.3]]}

                ;; top slant
                {:shape [:edge [0.3 0.8] [3 0.3]]}
                {:shape [:edge [3 0.3] [5.7 0.8]]}

                ;; bottom pit
                {:shape [:edge [1 8.7] [2.2 9.3]]}
                {:shape [:edge [4.2 8.7] [3 9.3]]}
                #_                {:shape [:edge [3 0.3] [5.7 0.8]]}


                {:shape [:edge [4.7 9.1] [5.0 9.3]]}
                {:shape [:edge [5.4 9.3] [5.7 9.1]]}
                ]}
    {:id :ball
     :position [5.2 8.8]
     :type :dynamic
     :fixtures [{:shape [:circle 0.30]
                 :density 0.2
                 :restitution 0.5}]}

    {:id :slider
     :position [5.2 10.6]
     :type :dynamic
     :fixtures [{:shape [:rect 0.2 2]
                 :density 10
                 :friction 1
                 }]}
    {:id :rail
     :position [5.5 9.3]
     :fixtures [{:shape [:edge [0 0] [0 1]]}]}
    ]
   [{:id :rail-slider-joint
     :type :prismatic
     :bodies [:rail :slider]
     :local-anchors [[-0.3 0]]
     :local-axis [0 1]
     :upper-translation 1.5
     :lower-translation 0
     :enable-limit? true
     }]


   )

  (b/apply-impulse!
   (b/find-by (world) :id :slider)
   [0 -70])



  (ancestors (class (first   (b/joints (world)))))
  (bean (first   (b/joints (world))))
  )
