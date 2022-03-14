(ns lambdaisland.cljbox2d.quil
  "Helpers for rendering a Box2D world to a Quil sketch

  Iteates over all bodies and draws them with simple drawing primitives using
  the current stroke style and color. Supply a custom `:draw` function to a
  body's `:user-data` to customize the drawing."
  #?(:clj (:require [lambdaisland.cljbox2d :as b]
                    [lambdaisland.cljbox2d.camera :as camera]
                    [lambdaisland.cljbox2d.math :as math]
                    [quil.core :as q])
     :cljs (:require [lambdaisland.cljbox2d :as b]
                     [lambdaisland.cljbox2d.camera :as camera]
                     [lambdaisland.cljbox2d.math :as math]
                     [quil.core :as q]
                     ["planck-js" :as planck]
                     ["planck-js/lib/common/Vec2" :as Vec2]
                     ["planck-js/lib/common/Mat22" :as Mat22]
                     ["planck-js/lib/World" :as World]
                     ["planck-js/lib/Body" :as Body]
                     ["planck-js/lib/Fixture" :as Fixture]
                     ["planck-js/lib/Joint" :as Joint]
                     ["planck-js/lib/Shape" :as Shape]
                     ["planck-js/lib/shape/CircleShape" :as CircleShape]
                     ["planck-js/lib/shape/EdgeShape" :as EdgeShape]
                     ["planck-js/lib/shape/PolygonShape" :as PolygonShape]
                     ["planck-js/lib/shape/ChainShape" :as ChainShape]
                     ["planck-js/lib/shape/BoxShape" :as BoxShape]
                     ["planck-js/lib/joint/DistanceJoint" :as DistanceJoint]
                     ["planck-js/lib/joint/FrictionJoint" :as FrictionJoint]
                     ["planck-js/lib/joint/GearJoint" :as GearJoint]
                     ["planck-js/lib/joint/MotorJoint" :as MotorJoint]
                     ["planck-js/lib/joint/MouseJoint" :as MouseJoint]
                     ["planck-js/lib/joint/PrismaticJoint" :as PrismaticJoint]
                     ["planck-js/lib/joint/PulleyJoint" :as PulleyJoint]
                     ["planck-js/lib/joint/RevoluteJoint" :as RevoluteJoint]
                     ["planck-js/lib/joint/RopeJoint" :as RopeJoint]
                     ["planck-js/lib/joint/WeldJoint" :as WeldJoint]
                     ["planck-js/lib/joint/WheelJoint" :as WheelJoint]))
  #?(:clj (:import (org.jbox2d.collision.shapes Shape
                                                ShapeType
                                                CircleShape
                                                EdgeShape
                                                PolygonShape
                                                ChainShape)
                   (org.jbox2d.common Vec2 Mat22 OBBViewportTransform)
                   (org.jbox2d.dynamics World
                                        Body
                                        BodyDef
                                        BodyType
                                        Filter
                                        Fixture
                                        FixtureDef)
                   (org.jbox2d.dynamics.joints ConstantVolumeJointDef
                                               DistanceJointDef
                                               FrictionJointDef
                                               GearJointDef
                                               Joint
                                               JointDef
                                               MotorJointDef
                                               MouseJointDef
                                               PrismaticJointDef
                                               PulleyJointDef
                                               RevoluteJointDef
                                               RopeJointDef
                                               WeldJointDef
                                               WheelJointDef)
                   (org.jbox2d.callbacks RayCastCallback
                                         ParticleRaycastCallback
                                         QueryCallback))))

(defprotocol IDraw
  (draw*! [entity] "Draw the given entity to the sketch using the current stroke style")
  (draw-shape! [shape body] "Draw a Box2D shape to the sketch using the current stroke style"))

(defn draw!
  "Draw a world, body, or fixture. Will iterate over nested entities, and honor a
  `:draw` function present in the `:user-data`."
  [entity]
  (if-let [draw (:draw (b/user-data entity))]
    (draw entity)
    (draw*! entity)))

(extend-protocol IDraw
  World
  (draw*! [w]
    (run! draw! (b/bodies w)))
  Body
  (draw*! [b]
    (run! draw! (b/fixtures b)))
  Fixture
  (draw*! [fixture]
    (if-let [draw (:draw (b/user-data fixture))]
      (draw fixture)
      (draw-shape! (b/shape fixture) (b/body fixture))))

  PolygonShape
  (draw-shape! [shape body]
    (q/begin-shape)
    (let [vs (map b/world->screen (b/world-vertices body shape))]
      (doseq [[^double x ^double y] vs]
        (q/vertex x y))
      (let [[^double x ^double y] (first vs)]
        (q/vertex x y)))
    (q/end-shape))

  CircleShape
  (draw-shape! [shape body]
    (let [[x y] (b/world->screen (b/world-point body (b/centroid shape)))
          radius (double (b/radius shape))
          matrix (b/transform b/*camera*)
          scale-x (.-x (.-ex matrix))
          scale-y (.-y (.-ey matrix))
          #_#_[^double scale-x ^double scale-y] (svd/get-scale matrix)]
      (q/push-matrix)
      (q/rotate (math/mat-angle matrix))
      (q/ellipse x y (* scale-x radius 2) (* scale-y radius 2))
      (q/pop-matrix)))

  EdgeShape
  (draw-shape! [shape body]
    (let [[[x1 y1] [x2 y2]] (map b/world->screen (b/world-vertices body shape))]
      (q/line x1 y1 x2 y2))))
