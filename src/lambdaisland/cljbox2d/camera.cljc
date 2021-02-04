(ns lambdaisland.cljbox2d.camera
  #?(:cljs (:require ["planck-js/lib/common/Vec2" :as Vec2]
                     ["planck-js/lib/common/Mat22" :as Mat22])
     :clj (:import (org.jbox2d.common Vec2 Mat22))))

(defprotocol ICamera
  (center [cam])
  (world->screen [cam vec])
  (screen->world [cam vec]))

(defn mat-invert [^Mat22 mat]
  #?(:clj (.invert mat)
     :cljs (.getInverse mat)))

(defn mat-mul [^Mat22 mat ^Vec2 vec]
  #?(:clj (.mul mat vec)
     :cljs (Mat22/mul mat vec)))

(defn mat-add [^Mat22 m1 ^Mat22 m2]
  #?(:clj (.add m1 m2)
     :cljs (Mat22/add m1 m2)))

(defn vec-add [^Vec2 v1 ^Vec2 v2]
  #?(:clj (.add v1 v2)
     :cljs (Vec2/add v1 v2)))

(defn vec-sub [^Vec2 v1 ^Vec2 v2]
  #?(:clj (.sub v1 v2)
     :cljs (Vec2/sub v1 v2)))

(defn mat-angle
  "Extract the angle from this matrix (assumed to be a rotation matrix)."
  [^Mat22 mat]
  #?(:clj (.getAngle mat)
     :cljs (Math/atan2 (.-y (.-ex mat)) (.-x (.-ex mat)))))

(defrecord Camera [^Mat22 transform ^Vec2 center ^Vec2 extents]
  ICamera
  (center [_]
    center)
  (world->screen [_ world]
    (vec-add (mat-mul transform (vec-sub world center)) extents))
  (screen->world [_ screen]
    (vec-add (mat-mul (mat-invert transform) (vec-sub screen extents)) center)))

(defn scale-transform [^double scale]
  (Mat22. (Vec2. scale 0) (Vec2. 0 scale)))

(defn camera [x y scale]
  (let [r (scale-transform scale)
        center (Vec2. x y)]
    (->Camera r center (Vec2. 0 0))))
