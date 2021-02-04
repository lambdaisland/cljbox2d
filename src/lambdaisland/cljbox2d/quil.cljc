(ns lambdaisland.cljbox2d.quil
  (:require [lambdaisland.cljbox2d :as b]
            [lambdaisland.cljbox2d.camera :as camera]
            [quil.core :as q])
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
                                         QueryCallback))
     :cljs (:require ["planck-js" :as planck]
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
                     ["planck-js/lib/joint/WheelJoint" :as WheelJoint])))

(defprotocol IDraw
  (draw*! [fixture])
  (draw-shape! [shape body]))

(defn draw! [entity]
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
    (let [vs (map (partial b/world->screen) (b/world-vertices body shape))]
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
      (q/rotate (camera/mat-angle matrix))
      (q/ellipse x y (* scale-x radius 2) (* scale-y radius 2))
      (q/pop-matrix)))


  )
