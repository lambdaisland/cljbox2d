(ns cljbox2d.tileset
  (:require [lambdaisland.cljbox2d :as b]
            [lambdaisland.cljbox2d.quil :as bq]
            [lambdaisland.quil-extras :as e]
            [quil.core :as q :include-macros true])
  (:import (processing.core PApplet PImage)))

(def pressed-keys (atom #{}))

(def debug? false)

(def tile-path "resources/0x72_DungeonTilesetII_v1.3.png")
(def fireball-path "resources/Fireball_68x9.png")

(def tile-set (e/load-grid tile-path {:width 16 :height 16 :scale 4}))
(def fireball-tiles (e/load-grid fireball-path {:width 67
                                                :height 9
                                                :pad-x 1
                                                :pad-y 0
                                                :scale 4}))

(def monster (e/tile-sequence tile-set [1 20 2 2] 8))
(def fireball (mapcat
               #(e/tile-sequence fireball-tiles [0 %] 10)
               (range 6)))

(defn millis []
  (System/currentTimeMillis))

(defn setup []
  (q/text-font (q/create-font "Roboto" 24 true)))

(defn draw-monster-body [body]
  (let [[x y] (b/world->screen (b/world-center body))
        {:keys [flipped?]} @body
        {:keys [touching?]} @(b/find-by body :id :foot-sensor)]
    (let [w (.-pixelWidth ^PImage (first monster))
          h (.-pixelHeight ^PImage (first monster))]
      (q/with-translation [x y]
        (q/with-rotation [(b/angle body)]
          (q/with-translation [(+ (- (double x))
                                  (- (double (/ w 2))))
                               (+ (- (double y))
                                  (- -10 (double (/ h 2))))]
            (q/push-matrix)
            (q/scale (if flipped? -1 1) 1)
            (if (and touching? (b/awake? body))
              (e/animate monster 9 (if flipped? (- (- x) w) x) y)
              (q/image (first monster) (if flipped? (- (- x) w) x) y))
            (q/pop-matrix))))
      (when debug?
        (q/no-fill)
        (bq/draw*! body)))))

(defn draw-bullet [body]
  (let [[x y] (b/world->screen (b/world-center body))
        {:keys [flipped?]} @body]
    (let [w (.-pixelWidth ^PImage (first fireball))
          h (.-pixelHeight ^PImage (first fireball))]
      (q/with-translation [x y]
        (q/with-translation [(+ (- (double x))
                                (- (double (/ w 2))))
                             (+ (- (double y))
                                (- -10 (double (/ h 2))))]
          (q/push-matrix)
          (q/scale (if flipped? 1 -1) 1)
          (e/animate fireball 30 (if flipped? x (- (- x) w)) y)
          (q/pop-matrix)))
      (when debug?
        (q/no-fill)
        (bq/draw*! body)))))

(defn on-begin-contact [contact]
  (when-let [feet (b/find-by contact :id :foot-sensor)]
    (swap! feet assoc :touching? true)))

(defn on-end-contact [contact]
  (when-let [feet (b/find-by contact :id :foot-sensor)]
    (swap! feet assoc :touching? false)))

(def world
  (-> (b/world 0 9.806)
      (b/populate [{:id       :ground
                    :position [6 9.8]
                    :fixtures [{:shape [:rect 100 0.4]}]}
                   {:id ::player
                    :type :dynamic
                    :position [6 8]
                    :fixed-rotation? true
                    :fixtures [{:shape [:rect 0.8 1.08]
                                :friction 5
                                :density 100}
                               {:id :foot-sensor
                                :sensor? true
                                :shape [:rect 0.5 0.1 [0 0.53]]
                                :user-data {:touching? true}}]
                    :draw #'draw-monster-body}
                   {:id :platform
                    :position [5 7]
                    :fixtures [{:shape [:rect 5 0.4]}]}])
      (b/listen! :begin-contact ::c #'on-begin-contact)
      (b/listen! :end-contact ::c #'on-end-contact)))

(defn shoot-fire! []
  (let [bullets (seq (b/find-all-by world :type :bullet))]
    (when (or (not bullets)
              (< 300 (- (millis) (apply max (map (comp :start-time deref) bullets)))))
      (let [player (b/find-by world :id ::player)
            [x y] (b/world-center player)
            {:keys [flipped?]} @player]
        (b/add-body world
                    {:position [((if flipped? - +) x 2) y]
                     :linear-velocity (if flipped? [-7 0] [7 0])
                     :type :kinematic
                     :bullet? true
                     :draw draw-bullet
                     :fixtures [{:shape [:rect 2.68 0.32]
                                 :density 100}]
                     :user-data {:type :bullet
                                 :start-time (millis)
                                 :flipped? flipped?}})))))

(defn control-player [[x y]]
  (let [player (b/find-by world :id ::player)
        [vx vy] (b/linear-velocity player)]
    (b/ctl! player :linear-velocity [(if (= 0 x) vx x)
                                     (if (= 0 y) vy y)])))

(defn process-keypress []
  (when (:left @pressed-keys)
    (b/alter-user-data! (b/find-by world :id ::player) assoc :flipped? true)
    (control-player [-3 0]))
  (when (:right @pressed-keys)
    (b/alter-user-data! (b/find-by world :id ::player) assoc :flipped? false)
    (control-player [3 0]))
  (when (and (:up @pressed-keys) (:touching? @(b/find-by world :id :foot-sensor)))
    (control-player [0 -8]))
  (when (:space @pressed-keys)
    (shoot-fire!)))

(defn clean-up-bullets []
  (let [now (millis)]
    (doseq [b (b/find-all-by world :type :bullet)]
      (when (< 1200 (- now (:start-time @b)))
        (b/destroy world b)))))

(defn draw []
  (clean-up-bullets)
  (process-keypress)
  (b/pan-x! (- (.-x (b/world-center (b/find-by world :id ::player)))
               (/ (q/width) 100 2)))
  (b/step-world world)
  (q/background 255)
  (bq/draw! world)
  (q/fill 100)
  (q/text (pr-str @pressed-keys) (- (q/width) 300) 150))

(q/defsketch tileset
  :host "app"
  :size [1200 1000]
  :setup setup
  :draw draw
  :frame-rate 60
  :key-pressed #(swap! pressed-keys conj (q/key-as-keyword))
  :key-released #(swap! pressed-keys disj (q/key-as-keyword)))
