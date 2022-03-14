(ns lambdaisland.cljbox2d.camera
  #?(:clj (:require [lambdaisland.cljbox2d.math :as math])
     :cljs (:require [lambdaisland.cljbox2d.math :as math]
                     ["planck-js/lib/common/Vec2" :as Vec2]
                     ["planck-js/lib/common/Mat22" :as Mat22]))
  #?(:clj (:import (org.jbox2d.common Vec2 Mat22))))

(defprotocol ICamera
  (center [cam])
  (world->screen [cam vec])
  (screen->world [cam vec]))

(defrecord Camera [^Mat22 transform ^Vec2 center ^Vec2 extents]
  ICamera
  (center [_]
    center)
  (world->screen [_ world]
    (math/vec-add (math/mat-mul transform (math/vec-sub world center)) extents))
  (screen->world [_ screen]
    (math/vec-add (math/mat-mul (math/mat-invert transform) (math/vec-sub screen extents)) center)))

(defn camera [x y scale]
  (let [r (math/scale-transform scale)
        center (Vec2. x y)]
    (->Camera r center (Vec2. 0 0))))

(defn pan! [camera x y]
  (.set (:center camera) (Vec2. x y))
  camera)

(defn pan-x! [camera x]
  (.set (:center camera) (Vec2. x (.-y (:center camera))))
  camera)

(defn pan-y! [camera y]
  (.set (:center camera) (Vec2. (.-x (:center camera)) y))
  camera)

(defn set-scale! [camera scale]
  (.set (:transform camera) (math/scale-transform scale))
  camera)
