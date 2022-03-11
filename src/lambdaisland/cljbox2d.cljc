(ns lambdaisland.cljbox2d
  (:require [lambdaisland.cljbox2d.data-printer :as data-printer]
            [lambdaisland.cljbox2d.camera :as camera]
            [lambdaisland.cljbox2d.math :as math])
  #?(:clj (:require [lambdaisland.cljbox2d.svd :as svd])
     :cljs (:require ["planck-js" :as planck]
                     ["planck-js/lib/common/Vec2" :as Vec2]
                     ["planck-js/lib/common/Mat22" :as Mat22]
                     ["planck-js/lib/common/Transform" :as Transform]
                     ["planck-js/lib/common/Rot" :as Rot]
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
  #?(:clj
     (:import (org.jbox2d.collision.shapes Shape
                                           ShapeType
                                           CircleShape
                                           EdgeShape
                                           PolygonShape
                                           ChainShape)
              (org.jbox2d.common Vec2 Mat22 Transform OBBViewportTransform Rot)
              (org.jbox2d.dynamics World
                                   Body
                                   BodyDef
                                   BodyType
                                   Filter
                                   Fixture
                                   FixtureDef)
              (org.jbox2d.dynamics.contacts Contact)
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
                                    QueryCallback
                                    ContactListener))))

#?(:clj
   (do
     (set! *warn-on-reflection* true)
     (set! *unchecked-math* :warn-on-boxed))
   :cljs
   (set! *warn-on-infer* true))

(def ^:dynamic *camera* (camera/camera 0 0 100)) ;; 1 meter  = 100px

(defprotocol IValue
  (value [_]))

(defprotocol IProperty
  (body ^Body [_])
  (angle [_])
  (position ^org.jbox2d.common.Vec2 [_])
  (fixture ^Fixture [_])
  (shape ^Shape [_])
  (centroid [_] "Local position of a shape/fixture inside a body")
  (bodies [_])
  (fixtures [_])
  (joints [_])
  (vertices [_])
  (user-data [_])
  (gravity [_])
  (density [_])
  (filter-data [_])
  (friction [_])
  (sensor? [_])
  (restitution [_])
  (transform ^Mat22 [_])
  (fixed-rotation? [_])
  (bullet? [_])
  (radius [_])
  (awake? [_])
  (linear-velocity [_])
  (angular-velocity [_])
  (world-center [_]))

(defprotocol IOperations
  (move-to! [_ v])
  (move-by! [_ v])
  (zoom*! [_ f])
  (ctl1! [entity k v] "Generically control properties")
  (alter-user-data*! [entity f args])
  (apply-force! [_ force] [_ force point])
  (apply-torque! [_ torque])
  (apply-impulse! [_ impulse wake?] [_ impulse point wake?])
  (apply-angular-impulse! [_ impulse]))

(defn alter-user-data! [entity f & args]
  (alter-user-data*! entity f args))

(defprotocol ICoerce
  (as-vec2 ^org.jbox2d.common.Vec2 [_]))

(extend-protocol IValue
  World
  (value [w]
    {:gravity (gravity w)
     :bodies (bodies w)})
  Vec2
  (value [v]
    [(.-x v) (.-y v)])
  Fixture
  (value [f]
    {:type (.getType f)
     :density (density f)
     :filter (filter-data f)
     :friction (friction f)
     :sensor? (sensor? f)
     :restitution (restitution f)
     :shape (shape f)
     :user-data (user-data f)})
  Body
  (value [b]
    {:position (position b)
     :angle (angle b)
     :fixtures (fixtures b)
     :fixed-rotation? (fixed-rotation? b)
     :bullet? (bullet? b)
     :transform (transform b)})

  Shape
  (value [s]
    {:childCount (.getChildCount s)
     :radius (.getRadius s)
     :type (.getType s)
     :vertices (vertices s)})
  PolygonShape
  (value [s]
    {:childCount (.getChildCount s)
     :normals (into [] (take (.-m_count s) (.-m_normals s)))
     :radius (.getRadius s)
     :type (.getType s)
     :vertices (vertices s)})

  Transform
  (value [t]
    [(.-p t) (.-q t)])
  Rot
  (value [r]
    {:sin (.-s r)
     :cos (.-c r)}))

#?(:clj
   (extend-protocol IValue
     BodyDef
     (value [b]
       {:active (.-active b)
        :allow-sleep? (.-allowSleep b)
        :angle (.-angle b)
        :angular-damping (.-angularDamping b)
        :angular-velocity (.-angularVelocity b)
        :awake (.-awake b)
        :bullet (.-bullet b)
        :fixed-rotation (.-fixedRotation b)
        :gravity-scale (.-gravityScale b)
        :linear-damping (.-linearDamping b)
        :linear-velocity (.-linearVelocity b)
        :position (.-position b)
        :type (.-type b)
        :user-data (.-userData b)})
     FixtureDef
     (value [f]
       {:density (.-density f)
        :filter (.-filter f)
        :friction (.-friction f)
        :sensor? (.-isSensor f)
        :restitution (.-restitution f)
        :shape (.-shape f)
        :user-data (.-userData f)})
     Filter
     (value [f]
       {:category-bits (.-categoryBits f)
        :mask-bits (.-maskBits f)
        :group-index (.-groupIndex f)})))

(data-printer/register-print World 'box2d/world value)
(data-printer/register-print Vec2 'box2d/vec2 value)
(data-printer/register-print Fixture 'box2d/fixture value)
(data-printer/register-print Body 'box2d/body value)
(data-printer/register-print PolygonShape 'box2d/polygon-shape value)
(data-printer/register-print Shape 'box2d/shape value)
(data-printer/register-print Transform 'box2d/transform value)
(data-printer/register-print Rot 'box2d/rot value)

#?(:clj
   (do
     (data-printer/register-print BodyDef 'box2d/body-def value)
     (data-printer/register-print FixtureDef 'box2d/fixture-def value)
     (data-printer/register-print Filter 'box2d/filter value)
     (data-printer/register-print BodyType 'box2d/body-type str)
     (data-printer/register-print ShapeType 'box2d/shape-type str)))

(defn vec2 ^Vec2 [^double x ^double y]
  (#?(:clj Vec2.
      :cljs planck/Vec2) x y))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Shapes

#?(:clj
   (defn- set-as-box
     ([^PolygonShape polygon-shape half-width half-height]
      (.setAsBox polygon-shape half-width half-height)
      polygon-shape)
     ([^PolygonShape polygon-shape half-width half-height center angle]
      (.setAsBox polygon-shape half-width half-height center angle)
      polygon-shape)))

(defn rectangle
  ([^double w ^double h]
   #?(:clj (set-as-box (PolygonShape.) (/ w 2) (/ h 2))
      :cljs (planck/Box (/ w 2) (/ h 2))))
  ([^double w ^double h center angle]
   #?(:clj (set-as-box (PolygonShape.) (/ w 2) (/ h 2) (as-vec2 center) angle)
      :cljs (planck/Box (/ w 2) (/ h 2) (as-vec2 center) angle))))

(defmulti make-shape (fn [s] (when (vector? s) (first s))))
(defmethod make-shape :default [s] s)

(defmethod make-shape :rect [[_ ^double width ^double height center angle]]
  (cond
    (and center angle)
    (rectangle width height center angle)
    center
    (rectangle width height center 0)
    :else
    (rectangle width height)))

(defmethod make-shape :circle [[_ a b]]
  (let [s (CircleShape.)]
    (if b
      (do
        (set! (.-m_p s) (as-vec2 a))
        (set! (.-m_radius s) b))
      (set! (.-m_radius s) a))
    s))

(defmethod make-shape :edge [[_ v1 v2]]
  (let [s (EdgeShape.)]
    (#?(:clj .set :cljs ._set) s (as-vec2 v1) (as-vec2 v2))
    s))

(defmethod make-shape :polygon [[_ & vs]]
  #?(:clj (doto (PolygonShape.)
            (.set (into-array Vec2 (map as-vec2 vs)) (count vs)))
     :cljs (PolygonShape. (into-array (map as-vec2 vs)))))

(defmethod make-shape :chain [[_ & vs]]
  #?(:clj (doto (ChainShape.)
            (.createChain (into-array Vec2 (map as-vec2 vs))
                          (count vs)))
     :cljs (ChainShape. (into-array (map as-vec2 vs)))))

(defmethod make-shape :loop [[_ & vs]]
  #?(:clj (doto (ChainShape.)
            (.createLoop (into-array Vec2 (map as-vec2 vs))
                         (count vs)))
     :cljs (ChainShape. (into-array (map as-vec2 vs)) true)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Body

(def body-type #?(:clj {:kinematic BodyType/KINEMATIC
                        :dynamic BodyType/DYNAMIC
                        :static BodyType/STATIC}
                  :cljs {:kinematic "kinematic"
                         :dynamic "dynamic"
                         :static "static"}))

(defn user-data-def [props]
  (cond
    (nil? (:user-data props))
    (select-keys props [:id :draw])
    (map? (:user-data props))
    (merge (:user-data props) (select-keys props [:id :draw]))
    :else
    (:user-data props)))

(defn body-def [{:keys [active? allow-sleep? angle angular-damping angular-velocity awake?
                        bullet? fixed-rotation? gravity-scale linear-damping linear-velocity position
                        type user-data]
                 :as props}]
  (let [b #?(:clj (BodyDef.) :cljs #js {})]
    (when (some? active?) (set! (.-active b) active?))
    (when (some? allow-sleep?) (set! (.-allowSleep b) allow-sleep?))
    (when (some? angle) (set! (.-angle b) angle))
    (when (some? angular-damping) (set! (.-angularDamping b) angular-damping))
    (when (some? angular-velocity) (set! (.-angularVelocity b) angular-velocity))
    (when (some? awake?) (set! (.-awake b) awake?))
    (when (some? bullet?) (set! (.-bullet b) bullet?))
    (when (some? fixed-rotation?) (set! (.-fixedRotation b) fixed-rotation?))
    (when (some? gravity-scale) (set! (.-gravityScale b) gravity-scale))
    (when (some? linear-damping) (set! (.-linearDamping b) linear-damping))
    (when (some? linear-velocity) (set! (.-linearVelocity b) (as-vec2 linear-velocity)))
    (when (some? position) (set! (.-position b) (as-vec2 position)))
    (when (some? type) (set! (.-type b) (get body-type type)))
    (set! (.-userData b) (user-data-def props))
    b))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Fixture

(defn fixture-def [{:keys [density filter friction sensor?
                           restitution shape user-data]
                    :as props}]
  (let [f #?(:clj (FixtureDef.) :cljs #js {})]
    (when (some? density) (set! (.-density f) density))
    (when (some? filter) (set! (.-filter f) filter))
    (when (some? friction) (set! (.-friction f) friction))
    (when (some? sensor?) (set! (.-isSensor f) sensor?))
    (when (some? restitution) (set! (.-restitution f) restitution))
    (when (some? shape) (set! (.-shape f) (make-shape shape)))
    (set! (.-userData f) (user-data-def props))
    f))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Joint

(defn- start-joint-def [type]
  #?(:clj
     (case type
       :constant-volume (ConstantVolumeJointDef.)
       :distance (DistanceJointDef.)
       :friction (FrictionJointDef.)
       :gear (GearJointDef.)
       :motor (MotorJointDef.)
       :mouse (MouseJointDef.)
       :prismatic (PrismaticJointDef.)
       :pulley (PulleyJointDef.)
       :revolute (RevoluteJointDef.)
       :rope (RopeJointDef.)
       :weld (WeldJointDef.)
       :wheel (WheelJointDef.))
     :cljs
     #js {}))

(defn- end-joint-def [type definition]
  #?(:clj
     definition
     :cljs
     (case type
       :distance (DistanceJoint. definition)
       :friction (FrictionJoint. definition)
       :gear (GearJoint. definition)
       :motor (MotorJoint. definition)
       :mouse (MouseJoint. definition)
       :prismatic (PrismaticJoint. definition)
       :pulley (PulleyJoint. definition)
       :revolute (RevoluteJoint. definition)
       :rope (RopeJoint. definition)
       :weld (WeldJoint. definition)
       :wheel (WheelJoint. definition))))

(defn joint-def [{:keys [type collide-connected? bodies joints
                         ;; constant-volume, distance, mouse
                         frequency damping
                         ;; distance, friction, prismatic
                         local-anchors length
                         ;; friction, motor, mouse
                         max-force max-torque
                         ;; gear, pulley
                         ratio
                         ;; motor
                         linear-offset angular-offset correction-factor
                         ;; prismatic
                         local-axis reference-angle enable-limit?
                         lower-translation upper-translation enable-motor?
                         max-motor-force motor-speed
                         ;; pulley
                         ground-anchors lengths
                         ;; Revolute
                         lower-angle upper-angle max-motor-torque
                         ;; rope
                         max-length
                         ]
                  :as props}]
  (let [#?(:clj ^JointDef j :cljs ^js j) (start-joint-def type)]
    (case type
      :constant-volume
      (let [j ^ConstantVolumeJointDef j]
        (when (some? frequency) (set! (.-frequencyHz j) frequency))
        (when (some? damping) (set! (.-dampingRatio j) damping))
        (when (seq bodies)
          (run! #(.addBody j %) bodies)))
      :distance
      (let [j ^DistanceJointDef j]
        (let [[aa ab] local-anchors]
          (when aa (set! (.-localAnchorA j) (as-vec2 aa)))
          (when ab (set! (.-localAnchorB j) (as-vec2 ab))))
        (when (some? frequency) (set! (.-frequencyHz j) frequency))
        (when (some? damping) (set! (.-dampingRatio j) damping))
        (when (some? length) (set! (.-length j) length)))
      :friction
      (let [j ^FrictionJointDef j]
        (let [[aa ab] local-anchors]
          (when aa (set! (.-localAnchorA j) (as-vec2 aa)))
          (when ab (set! (.-localAnchorB j) (as-vec2 ab))))
        (when (some? max-force) (set! (.-maxForce j) max-force))
        (when (some? max-torque) (set! (.-maxTorque j) max-torque)))
      :gear
      (let [j ^GearJointDef j]
        (let [[j1 j2] joints]
          (set! (.-joint1 j) j1)
          (set! (.-joint2 j) j2))
        (when (some? ratio) (set! (.-ratio j) ratio)))
      :motor
      (let [j ^MotorJointDef j]
        (when (some? linear-offset) (set! (.-linearOffset j) (as-vec2 linear-offset)))
        (when (some? angular-offset) (set! (.-angularOffset j) angular-offset))
        (when (some? max-force) (set! (.-maxForce j) max-force))
        (when (some? max-torque) (set! (.-maxTorque j) max-torque))
        (when (some? correction-factor) (set! (.-correctionFactor j) correction-factor)))
      :mouse
      (let [j ^MouseJointDef j]
        (when (some? max-force) (set! (.-maxForce j) max-force))
        (when (some? frequency) (set! (.-frequencyHz j) frequency))
        (when (some? damping) (set! (.-dampingRatio j) damping)))
      :prismatic
      (let [j ^PrismaticJointDef j]
        (let [[aa ab] local-anchors]
          (when aa (set! (.-localAnchorA j) (as-vec2 aa)))
          (when ab (set! (.-localAnchorB j) (as-vec2 ab))))
        (when (some? local-axis) (set! (.-localAxisA j) (as-vec2 local-axis)))
        (when (some? reference-angle) (set! (.-referenceAngle j) reference-angle))
        (when (some? enable-limit?) (set! (.-enableLimit j) enable-limit?))
        (when (some? lower-translation) (set! (.-lowerTranslation j) lower-translation))
        (when (some? upper-translation) (set! (.-upperTranslation j) upper-translation))
        (when (some? enable-motor?) (set! (.-enableMotor j) enable-motor?))
        (when (some? max-motor-force) (set! (.-maxMotorForce j) max-motor-force))
        (when (some? motor-speed) (set! (.-motorSpeed j) motor-speed)))
      :pulley
      (let [j ^PulleyJointDef j]
        (let [[aa ab] local-anchors]
          (when aa (set! (.-localAnchorA j) (as-vec2 aa)))
          (when ab (set! (.-localAnchorB j) (as-vec2 ab))))
        (let [[aa ab] ground-anchors]
          (when aa (set! (.-groundAnchorA j) (as-vec2 aa)))
          (when ab (set! (.-groundAnchorB j) (as-vec2 ab))))
        (let [[la lb] lengths]
          (when la (set! (.-lengthA j) la))
          (when lb (set! (.-lengthB j) lb)))
        (when (some? ratio) (set! (.-ratio j) ratio)))
      :revolute
      (let [j ^RevoluteJointDef j]
        (let [[aa ab] local-anchors]
          (when aa (set! (.-localAnchorA j) (as-vec2 aa)))
          (when ab (set! (.-localAnchorB j) (as-vec2 ab))))
        (when (some? reference-angle) (set! (.-referenceAngle j) reference-angle))
        (when (some? lower-angle) (set! (.-lowerAngle j) lower-angle))
        (when (some? upper-angle) (set! (.-upperAngle j) upper-angle))
        (when (some? max-motor-torque) (set! (.-maxMotorTorque j) max-motor-torque))
        (when (some? motor-speed) (set! (.-motorSpeed j) motor-speed))
        (when (some? enable-limit?) (set! (.-enableLimit j) enable-limit?))
        (when (some? enable-motor?) (set! (.-enableMotor j) enable-motor?)))
      :rope
      (let [j ^RopeJointDef j]
        (let [[aa ab] local-anchors]
          (when aa (set! (.-localAnchorA j) (as-vec2 aa)))
          (when ab (set! (.-localAnchorB j) (as-vec2 ab))))
        (when (some? max-length) (set! (.-maxLength j) max-length)))
      :weld
      (let [j ^WeldJointDef j]
        (let [[aa ab] local-anchors]
          (when aa (set! (.-localAnchorA j) (as-vec2 aa)))
          (when ab (set! (.-localAnchorB j) (as-vec2 ab))))
        (when (some? reference-angle) (set! (.-referenceAngle j) reference-angle))
        (when (some? frequency) (set! (.-frequencyHz j) frequency))
        (when (some? damping) (set! (.-dampingRatio j) damping)))
      :wheel
      (let [j ^WheelJointDef j]
        (let [[aa ab] local-anchors]
          (when aa (set! (.-localAnchorA j) (as-vec2 aa)))
          (when ab (set! (.-localAnchorB j) (as-vec2 ab))))
        (when (some? local-axis) (set! (.-localAxisA j) (as-vec2 local-axis)))
        (when (some? enable-motor?) (set! (.-enableMotor j) enable-motor?))
        (when (some? max-motor-torque) (set! (.-maxMotorTorque j) max-motor-torque))
        (when (some? motor-speed) (set! (.-motorSpeed j) motor-speed))))
    (set! (.-userData j) (user-data-def props))
    (let [[bodyA bodyB] bodies]
      (when bodyA (set! (.-bodyA j) bodyA))
      (when bodyB (set! (.-bodyB j) bodyB)))
    (set! (.-collideConnected j) (boolean collide-connected?))
    (end-joint-def type j)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Listeners

;; Some differences here between jBox2d and planck. The former only allows
;; setting a single ContactListener, the latter has replaced the ContactListener
;; with JavaScript style event registration (world.on("begin-contact",
;; function(contact) {...})). We turn this into an interface that is more
;; idiomatic for Clojure, allowing multiple listeners identified by keyword,
;; similar to watches on an atom. This also makes them quite suitable for a REPL
;; driven workflow, since it's easy to continuously replace a specific listener.

(comment
  ;; listeners looks like:
  (def listeners (atom {:begin-contact {:my-key (fn [,,,])}})))

(defn- dispatch-event [listeners event & args]
  (doseq [f (vals (get @listeners event))]
    (apply f args)))

#?(:clj
   (defrecord ContactListenerFanout [listeners]
     ContactListener
     (beginContact [this contact]
       (dispatch-event listeners :begin-contact contact))
     (endContact [this contact]
       (dispatch-event listeners :end-contact contact))
     (preSolve [this contact old-manifold]
       (dispatch-event listeners :pre-solve contact old-manifold))
     (postSolve [this contact impulse]
       (dispatch-event listeners :post-solve contact impulse))))

(defn setup-listener-fanout! [^World world]
  #?(:clj
     (.setContactListener world (->ContactListenerFanout (atom {})))
     :cljs
     (let [listeners (atom {})]
       (set! (.-CLJS_LISTENERS world) listeners)
       (.on world "begin-contact" #(dispatch-event listeners :begin-contact %1))
       (.on world "end-contact" #(dispatch-event listeners :end-contact %1))
       (.on world "pre-solve" #(dispatch-event listeners :pre-solve %1 %2))
       (.on world "post-solve" #(dispatch-event listeners :post-solve %1 %2)))))

(defn listen!
  "Listen for world events, `event` is one
  of :begin-contact, :end-contact, :pre-solve, :post-solve. `key` is a key you
  choose to identify this listener. Calling [[listen!]] again with the same
  event+key will replace the old listener. The key can also be used
  to [[unlisten!]]

  Returns the world instance for easy threading."
  [^World world event key f]
  (swap! #?(:clj (:listeners (.. world getContactManager -m_contactListener))
            :cljs (.-CLJS_LISTENERS world))
         assoc-in
         [event key]
         f)
  world)

(defn unlisten!
  "Remove a specific event listener"
  [^World world event key]
  (swap! #?(:clj (:listeners (.. world getContactManager -m_contactListener))
            :cljs (.-CLJS_LISTENERS world))
         update event
         dissoc key)
  world)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Build world

(defn add-fixture
  ([^Body body f]
   #?(:clj (cond
             (instance? Shape f)
             (add-fixture body f 0)
             (map? f)
             (add-fixture body (fixture-def f))
             (instance? FixtureDef f)
             (.createFixture body ^FixtureDef f))
      :cljs (cond
              (instance? Shape f)
              (add-fixture body f 0)
              (map? f)
              (.createFixture body (fixture-def f)))))
  ([^Body body shape density]
   (.createFixture body ^Shape shape ^double density)))

(defn add-body [^World world props]
  (let [body (.createBody world (body-def props))]
    (run! (partial add-fixture body) (:fixtures props))
    body))

(defn indexed [entities]
  (into {} (map (juxt (comp :id user-data) identity)) entities))

(defn add-joint [^World world props]
  (let [id->body (indexed (bodies world))
        id->joint (indexed (joints world))
        props (-> props
                  (update :bodies (partial map id->body))
                  (update :joints (partial map id->joint)))]
    (.createJoint world (joint-def props))))

(defn world [gravity-x gravity-y]
  (let [world (World. (vec2 gravity-x gravity-y))]
    (setup-listener-fanout! world)

    world))

(defn populate
  ([world bodies]
   (populate world bodies nil))
  ([world bodies joints]
   (run! (partial add-body world) bodies)
   (run! (partial add-joint world) joints)
   world))

(defn destroy [^World world object]
  (cond
    (instance? Joint object)
    (.destroyJoint world ^Joint object))
  (cond
    (instance? Body object)
    (.destroyBody world ^Joint object)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Use world

(defn step-world
  ([world]
   (step-world world (/ 1 60)))
  ([world timestep]
   (step-world world timestep 4 2))
  ([^World world timestep velocity-iterations position-iterations]
   (.step world timestep velocity-iterations position-iterations)))

(defn- get-next [x]
  #?(:clj (cond (instance? Body x)
                (.getNext ^Body x)
                (instance? Fixture x)
                (.getNext ^Fixture x)
                (instance? Joint x)
                (.getNext ^Joint x))
     :cljs (.getNext ^js x)))

(defn linked-list-seq [x]
  (when x
    (loop [x x
           xs [x]]
      (if-let [x (get-next x)]
        (recur x (conj xs x))
        xs))))

(defn world->screen
  ([vec]
   (camera/world->screen *camera* vec))
  ([camera vec]
   (camera/world->screen camera vec)))

(defn screen->world
  ([vec]
   (camera/screen->world *camera* vec))
  ([camera vec]
   (camera/screen->world camera vec)))

(defn world-point [^Body body vec]
  (.getWorldPoint body (as-vec2 vec)))

(defn world-vertices
  ([fixture]
   (world-vertices (body fixture) fixture))
  ([^Body body fixture-or-shape]
   (let [vertices (vertices fixture-or-shape)]
     (map (partial world-point body) vertices))))

(defn find-by
  "Find a body or fixture with a given user-data property,
  e.g. (find-by world :id :player)"
  [container k v]
  (if (sequential? container)
    (some #(find-by % k v) container)
    (reduce #(when (= (get (user-data %2) k) v)
               (reduced %2))
            nil
            (concat (bodies container)
                    (fixtures container)))))

(defn find-all-by
  "Find all bodies or fixtures with a given user-data property,
  e.g. (find-by world :type :npc)"
  [container k v]
  (if (sequential? container)
    (mapcat #(find-all-by % k v))
    (filter (comp #{v} #(get % k) user-data)
            (concat (bodies container)
                    (fixtures container)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Querying

#?(:clj
   (defn raycast-callback ^RayCastCallback [f]
     (if (instance? RayCastCallback f)
       f
       (reify RayCastCallback
         (reportFixture [_ fixture point normal fraction]
           (f fixture point normal fraction))))))

#?(:clj
   (defn particle-raycast-callback ^ParticleRaycastCallback [f]
     (if (instance? ParticleRaycastCallback f)
       f
       (reify ParticleRaycastCallback
         (reportParticle [_ index point normal fraction]
           (f index point normal fraction))))))

(defn raycast
  ([^World world rcb point1 point2]
   #?(:clj
      (.raycast world
                (raycast-callback rcb)
                (as-vec2 point1)
                (as-vec2 point2))
      :cljs
      (.raycast world
                (as-vec2 point1)
                (as-vec2 point2)
                rcb)))
  #?(:clj ([^World world rcb pcb point1 point2]
           (.raycast world
                     (raycast-callback rcb)
                     (particle-raycast-callback pcb)
                     (as-vec2 point1)
                     (as-vec2 point2)))))

#?(:clj (defn particle-raycast [^World world pcb point1 point2]
          (.raycast world
                    #?(:clj (particle-raycast-callback pcb) :cljs pcb)
                    (as-vec2 point1)
                    (as-vec2 point2))))

(defn raycast-seq [^World world point1 point2]
  (let [result (volatile! (transient []))]
    (raycast world
             (fn [fixture _ _ _]
               (vswap! result conj! fixture)
               1)
             point1 point2)
    (persistent! @result)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Properties and Operations

(defn ctl! [entity & kvs]
  (doseq [[k v] (partition 2 kvs)]
    (ctl1! entity k v)))

(defn zoom!
  ([amount]
   (zoom*! *camera* amount))
  ([camera amount]
   (zoom*! camera amount)))

(defn pan!
  ([x y]
   (camera/pan! *camera* x y))
  ([camera x y]
   (camera/pan! camera x y)))

(defn pan-x!
  ([x]
   (camera/pan-x! *camera* x))
  ([camera x]
   (camera/pan-x! camera x)))

(defn pan-y!
  ([y]
   (camera/pan-y! *camera* y))
  ([camera y]
   (camera/pan-y! camera y)))

(defn set-viewport! [x y scale]
  (camera/pan! *camera* x y)
  (camera/set-scale! *camera* scale))

(extend-protocol IProperty
  World
  (bodies [w]
    (linked-list-seq (.getBodyList w)))
  (joints [w]
    (linked-list-seq (.getJointList w)))
  (fixtures [w]
    (mapcat fixtures (bodies w)))
  (vertices [w]
    (mapcat vertices (fixtures w)))
  (gravity [w]
    (.getGravity w))
  (user-data [w] nil)

  Body
  (body [b]
    b)
  (angle [b]
    (.getAngle b))
  (position [b]
    (.getPosition b))
  (bodies [b]
    [b])
  (fixtures [b]
    (linked-list-seq (.getFixtureList b)))
  (vertices [b]
    (mapcat vertices (fixtures b)))
  (transform [b]
    (.getTransform b))
  (fixed-rotation? [b]
    (.isFixedRotation b))
  (bullet? [b]
    (.isBullet b))
  (user-data [b]
    (.-m_userData b))
  (awake? [b]
    (.isAwake b))
  (linear-velocity [b]
    (.-m_linearVelocity b))
  (angular-velocity [b]
    (.-m_angularVelocity b))
  (world-center [b]
    (.getWorldCenter b))

  Fixture
  (body [f]
    (.-m_body f))
  (bodies [f]
    [(body f)])
  (fixtures [f]
    [f])
  (angle [f]
    (angle (body f)))
  (shape [f]
    (.-m_shape f))
  (vertices [f]
    (vertices (shape f)))
  (density [f]
    (.-m_density f))
  (friction [f]
    (.-m_friction f))
  (restitution [f]
    (.-m_restitution f))
  (sensor? [f]
    (.-m_isSensor f))
  (user-data [f]
    (.-m_userData f))
  (filter-data [f]
    #?(:clj (let [filter (.getFilterData f)]
              {:category-bits (.-categoryBits filter)
               :mask-bits (.-maskBits filter)
               :group-index (.-groupIndex filter)})
       :cljs
       {:category-bits (.-m_filterCategoryBits f)
        :mask-bits (.-m_filterMaskBits f)
        :group-index (.-m_filterGroupIndex f)}))

  PolygonShape
  (vertices [s]
    (into [] (take (.-m_count s) (.-m_vertices s))))
  (centroid [s]
    (.-m_centroid s))

  CircleShape
  (vertices [s]
    [(.-m_p s)])
  (centroid [s]
    (.-m_p s))
  (radius [s]
    (.-m_radius s))

  EdgeShape
  (vertices [s]
    [(.-m_vertex1 s) (.-m_vertex2 s)])
  (centroid [s]
    (doto ^Vec2 (vec2 0 0)
      (.addLocal (.-m_vertex0 s))
      (.addLocal (.-m_vertex1 s))
      (.mulLocal 0.5)))

  ChainShape
  (vertices [s]
    (.-m_vertices s))

  lambdaisland.cljbox2d.camera.Camera
  (position [c]
    (camera/center c))
  (transform [c]
    (.-transform c))

  Joint
  (user-data [j]
    (.-m_userData j))

  Contact
  (fixtures [c]
    [(.-m_fixtureA c) (.-m_fixtureB c)])
  (bodies [c]
    (map body (fixtures c))))

(extend-protocol IOperations
  World
  (ctl1! [w k v]
    (case k
      :allow-sleep? (.setAllowSleep w v)
      :sub-stepping? (.setSubStepping w v)
      :gravity (.setGravity w (as-vec2 v))
      :particle-max-count (.setParticleMaxCount w v)
      :particle-density (.setParticleDensity w v)
      :particle-gravity-scale (.setParticleGravityScale w v)
      :particle-dampint (.setParticleDamping w v)
      :particle-radius (.setParticleRadius w v)))

  Body
  (ctl1! [b k v]
    (case k
      :linear-velocity (.setLinearVelocity b (as-vec2 v))
      :angular-velocity (.setAngularVelocity b v)
      :transform (.setTransform b (as-vec2 (first v)) (second v))
      :position (.setTransform b (as-vec2 v) (.getAngle b))
      :gravity-scale (.setGravityScale b v)
      :allow-sleep? (.setSleepingAllowed b v)
      :awake? (.setAwake b v)
      :active? (.setActive b v)
      :fixed-rotation? (.setFixedRotation b v)
      :bullet? (.setBullet b v)))
  (alter-user-data*! [b f args]
    (.setUserData b (apply f (.-m_userData b) args)))
  (apply-force!
    ([b force]
     (.applyForceToCenter b (as-vec2 force)))
    ([b force point]
     (.applyForce b (as-vec2 force) (as-vec2 point))))
  (apply-torque! [b torque]
    (.applyTorque b torque))
  (apply-impulse!
    ([b impulse wake?]
     (.applyLinearImpulse b (as-vec2 impulse) (.getWorldCenter b) wake?))
    ([b impulse point wake?]
     (.applyLinearImpulse b (as-vec2 impulse) (as-vec2 point) wake?)))
  (apply-angular-impulse! [b impulse]
    (.applyAngularImpulse b impulse))

  lambdaisland.cljbox2d.camera.Camera
  (move-to! [camera center]
    (.set ^Vec2 (.-center camera) (as-vec2 center))
    camera)
  (move-by! [camera offset]
    (.set ^Vec2 (.-center camera) (math/vec-add (.-center camera)
                                                (as-vec2 offset)))
    camera)
  (zoom*! [camera amount]
    (.set ^Mat22 (.-transform camera)
          (math/mat-add (.-transform camera)
                        (math/scale-transform amount))))

  Fixture
  (alter-user-data*! [fixt f args]
    (.setUserData fixt (apply f (.-m_userData fixt) args)))

  Joint
  (alter-user-data*! [j f args]
    (.setUserData j (apply f (.-m_userData j) args))))

(extend-protocol ICoerce
  #?(:clj Vec2 :cljs planck/Vec2)
  (as-vec2 [v] v)
  #?(:clj clojure.lang.Indexed :cljs cljs.core/PersistentVector)
  (as-vec2 [[x y]]
    (vec2 x y)))

#?(:cljs
   (extend-type planck/Vec2
     ;; Allow destructuring. In Clojure we use a patched jBox2D for this.
     cljs.core/IIndexed
     (-nth
       ([v n]
        (case n 0 (.-x v) 1 (.-y v)))
       ([v n not-found]
        (case n 0 (.-x v) 1 (.-y v) not-found)))))
