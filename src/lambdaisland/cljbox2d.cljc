(ns lambdaisland.cljbox2d
  #_(:require [lambdaisland.data-printer :as data-printer]
              [lambdaisland.jbox2d.svd :as svd]
              [quil.core :as q])
  #_(:import (org.jbox2d.collision.shapes Shape
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
                                   QueryCallback)))

;; (set! *warn-on-reflection* true)
;; (set! *unchecked-math* :warn-on-boxed)

;; (defn camera [^double world-x ^double world-y ^double scale]
;;   (doto (OBBViewportTransform.)
;;     (.setCamera world-x world-y scale)))

;; (def ^{:dynamic true :tag OBBViewportTransform}
;;   *camera* (camera 0 0 100)) ;; 1 meter  = 100px

;; (defprotocol IValue
;;   (value [_]))

;; (defprotocol IProperty
;;   (body ^Body [_])
;;   (angle ^double [_])
;;   (position ^org.jbox2d.common.Vec2 [_])
;;   (fixture ^Fixture [_])
;;   (shape ^Shape [_])
;;   (centroid [_] "Local position of a shape/fixture inside a body")
;;   (bodies [_])
;;   (fixtures [_])
;;   (joints [_])
;;   (vertices [_])
;;   (user-data [_]))

;; (defprotocol IOperations
;;   (move-to! [_ v])
;;   (move-by! [_ v])
;;   (zoom! [_ f])
;;   (draw! [fixture])
;;   (draw-shape! [shape body])
;;   (ctl1! [entity k v] "Generically control properties")
;;   (alter-user-data! [entity f & args])
;;   (apply-force! [_ force] [_ force point])
;;   (apply-torque! [_ torque])
;;   (apply-impulse! [_ impulse point wake?])
;;   (apply-angular-impulse! [_ impulse]))

;; (defprotocol ICoerce
;;   (as-vec2 ^org.jbox2d.common.Vec2 [_]))

;; (extend-protocol IValue
;;   World
;;   (value [w]
;;     {:gravity (.getGravity w)
;;      :bodies (bodies w)})
;;   Vec2
;;   (value [v]
;;     [(.-x v) (.-y v)])
;;   BodyDef
;;   (value [b]
;;     {:active (.-active b)
;;      :allow-sleep? (.-allowSleep b)
;;      :angle (.-angle b)
;;      :angular-damping (.-angularDamping b)
;;      :angular-velocity (.-angularVelocity b)
;;      :awake (.-awake b)
;;      :bullet (.-bullet b)
;;      :fixed-rotation (.-fixedRotation b)
;;      :gravity-scale (.-gravityScale b)
;;      :linear-damping (.-linearDamping b)
;;      :linear-velocity (.-linearVelocity b)
;;      :position (.-position b)
;;      :type (.-type b)
;;      :user-data (.-userData b)})
;;   Fixture
;;   (value [f]
;;     {:type (.getType f)
;;      :density (.getDensity f)
;;      :filter (.getFilterData f)
;;      :friction (.getFriction f)
;;      :sensor? (.isSensor f)
;;      :restitution (.getRestitution f)
;;      :shape (.getShape f)
;;      :user-data (.getUserData f)})
;;   FixtureDef
;;   (value [f]
;;     {:density (.-density f)
;;      :filter (.-filter f)
;;      :friction (.-friction f)
;;      :sensor? (.-isSensor f)
;;      :restitution (.-restitution f)
;;      :shape (.-shape f)
;;      :user-data (.-userData f)})
;;   Body
;;   (value [b]
;;     {:position (.getPosition b)
;;      :angle (.getAngle b)
;;      :fixtures (fixtures b)
;;      :fixed-rotation? (.isFixedRotation b)
;;      :transform (.getTransform b)})
;;   Filter
;;   (value [f]
;;     {:category-bits (.-categoryBits f)
;;      :mask-bits (.-maskBits f)
;;      :group-index (.-groupIndex f)})
;;   Shape
;;   (value [s]
;;     {:childCount (.getChildCount s)
;;      :radius (.getRadius s)
;;      :type (.getType s)})
;;   PolygonShape
;;   (value [s]
;;     {:childCount (.getChildCount s)
;;      :normals (into [] (take (.getVertexCount s) (.getNormals s)))
;;      :radius (.getRadius s)
;;      :type (.getType s)
;;      :vertexCount (.getVertexCount s)
;;      :vertices (into [] (take (.getVertexCount s) (.getVertices s)))}))

;; (data-printer/register-type-printer World 'jbox2d/world value)
;; (data-printer/register-type-printer Vec2 'jbox2d/vec2 value)
;; (data-printer/register-type-printer BodyDef 'jbox2d/body-def value)
;; (data-printer/register-type-printer Fixture 'jbox2d/fixture value)
;; (data-printer/register-type-printer FixtureDef 'jbox2d/fixture-def value)
;; (data-printer/register-type-printer Body 'jbox2d/body value)
;; (data-printer/register-type-printer Filter 'jbox2d/filter value)
;; (data-printer/register-type-printer PolygonShape 'jbox2d/polygon-shape value)
;; (data-printer/register-type-printer Shape 'jbox2d/shape value)

;; (data-printer/register-type-printer BodyType 'jbox2d/body-type str)
;; (data-printer/register-type-printer ShapeType 'jbox2d/shape-type str)

;; (defn vec2 ^Vec2 [^double x ^double y]
;;   (Vec2. x y))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;; Shapes

;; (defn set-as-box
;;   ([^PolygonShape polygon-shape half-width half-height]
;;    (.setAsBox polygon-shape half-width half-height)
;;    polygon-shape)
;;   ([^PolygonShape polygon-shape half-width half-height center angle]
;;    (.setAsBox polygon-shape half-width half-height (as-vec2 center) angle)
;;    polygon-shape))

;; (defn rectangle
;;   ([^double w ^double h]
;;    (set-as-box (PolygonShape.) (/ w 2) (/ h 2)))
;;   ([^double w ^double h center rectangle]
;;    (set-as-box (PolygonShape.) (/ w 2) (/ h 2) center rectangle)))

;; (defmulti make-shape (fn [s] (when (vector? s) (first s))))
;; (defmethod make-shape :default [s] s)

;; (defmethod make-shape :rect [[_ ^double width ^double height center angle]]
;;   (if (and center angle)
;;     (rectangle width height center angle)
;;     (rectangle width height)))

;; (defmethod make-shape :circle [[_ radius]]
;;   (doto (CircleShape.)
;;     (.setRadius radius)))

;; (defmethod make-shape :edge [[_ v1 v2]]
;;   (doto (EdgeShape.)
;;     (.set (as-vec2 v1) (as-vec2 v2))))

;; (defmethod make-shape :polygon [[_ & vs]]
;;   (doto (PolygonShape.)
;;     (.set (into-array Vec2 (map as-vec2 vs)) (count vs))))

;; (defmethod make-shape :chain [[_ & vs]]
;;   (doto (ChainShape.)
;;     (.createChain (into-array Vec2 (map as-vec2 vs))
;;                   (count vs))))

;; (defmethod make-shape :loop [[_ & vs]]
;;   (doto (ChainShape.)
;;     (.createLoop (into-array Vec2 (map as-vec2 vs))
;;                  (count vs))))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;; Body

;; (def body-type {:kinematic BodyType/KINEMATIC
;;                 :dynamic BodyType/DYNAMIC
;;                 :static BodyType/STATIC})

;; (defn user-data-def [props]
;;   (or (:user-data props)
;;       (select-keys props [:id :draw])))

;; (defn body-def [{:keys [active? allow-sleep? angle angular-damping angular-velocity awake?
;;                         bullet? fixed-rotation? gravity-scale linear-damping linear-velocity position
;;                         type user-data]
;;                  :as props}]
;;   (let [b (BodyDef.)]
;;     (when (some? active?) (set! (.-active b) active?))
;;     (when (some? allow-sleep?) (set! (.-allowSleep b) allow-sleep?))
;;     (when (some? angle) (set! (.-angle b) angle))
;;     (when (some? angular-damping) (set! (.-angularDamping b) angular-damping))
;;     (when (some? angular-velocity) (set! (.-angularVelocity b) angular-velocity))
;;     (when (some? awake?) (set! (.-awake b) awake?))
;;     (when (some? bullet?) (set! (.-bullet b) bullet?))
;;     (when (some? fixed-rotation?) (set! (.-fixedRotation b) fixed-rotation?))
;;     (when (some? gravity-scale) (set! (.-gravityScale b) gravity-scale))
;;     (when (some? linear-damping) (set! (.-linearDamping b) linear-damping))
;;     (when (some? linear-velocity) (set! (.-linearVelocity b) linear-velocity))
;;     (when (some? position) (set! (.-position b) (as-vec2 position)))
;;     (when (some? type) (set! (.-type b) (get body-type type)))
;;     (set! (.-userData b) (user-data-def props))
;;     b))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;; Fixture

;; (defn fixture-def [{:keys [density filter friction sensor?
;;                            restitution shape user-data]
;;                     :as props}]
;;   (let [f (FixtureDef.)]
;;     (when (some? density) (set! (.-density f) density))
;;     (when (some? filter) (set! (.-filter f) filter))
;;     (when (some? friction) (set! (.-friction f) friction))
;;     (when (some? sensor?) (set! (.-isSensor f) sensor?))
;;     (when (some? restitution) (set! (.-restitution f) restitution))
;;     (when (some? shape) (set! (.-shape f) (make-shape shape)))
;;     (set! (.-userData f) (user-data-def props))
;;     f))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;; Joint

;; (defn joint-def [{:keys [type collide-connected? bodies joints
;;                          ;; constant-volume, distance, mouse
;;                          frequency damping
;;                          ;; distance, friction, prismatic
;;                          local-anchors length
;;                          ;; friction, motor, mouse
;;                          max-force max-torque
;;                          ;; gear, pulley
;;                          ratio
;;                          ;; motor
;;                          linear-offset angular-offset correction-factor
;;                          ;; prismatic
;;                          local-axis reference-angle enable-limit?
;;                          lower-translation upper-translation enable-motor?
;;                          max-motor-force motor-speed
;;                          ;; pulley
;;                          ground-anchors lengths
;;                          ;; Revolute
;;                          lower-angle upper-angle max-motor-torque
;;                          ;; rope
;;                          max-length
;;                          ]
;;                   :as props}]
;;   (let [^JointDef j
;;         (case type
;;           :constant-volume
;;           (let [j (ConstantVolumeJointDef.)]
;;             (when (some? frequency) (set! (.-frequencyHz j) frequency))
;;             (when (some? damping) (set! (.-dampingRatio j) damping))
;;             (when (seq bodies)
;;               (run! #(.addBody j %) bodies))
;;             j)
;;           :distance
;;           (let [j (DistanceJointDef.)]
;;             (let [[aa ab] local-anchors]
;;               (when aa (set! (.-localAnchorA j) (as-vec2 aa)))
;;               (when ab (set! (.-localAnchorB j) (as-vec2 ab))))
;;             (when (some? frequency) (set! (.-frequencyHz j) frequency))
;;             (when (some? damping) (set! (.-dampingRatio j) damping))
;;             (when (some? length) (set! (.-length j) length))
;;             j)
;;           :friction
;;           (let [j (FrictionJointDef.)]
;;             (let [[aa ab] local-anchors]
;;               (when aa (set! (.-localAnchorA j) (as-vec2 aa)))
;;               (when ab (set! (.-localAnchorB j) (as-vec2 ab))))
;;             (when (some? max-force) (set! (.-maxForce j) max-force))
;;             (when (some? max-torque) (set! (.-maxTorque j) max-torque))
;;             j)
;;           :gear
;;           (let [j (GearJointDef.)]
;;             (let [[j1 j2] joints]
;;               (set! (.-joint1 j) j1)
;;               (set! (.-joint2 j) j2))
;;             (when (some? ratio) (set! (.-ratio j) ratio))
;;             j)
;;           :motor
;;           (let [j (MotorJointDef.)]
;;             (when (some? linear-offset) (set! (.-linearOffset j) (as-vec2 linear-offset)))
;;             (when (some? angular-offset) (set! (.-angularOffset j) angular-offset))
;;             (when (some? max-force) (set! (.-maxForce j) max-force))
;;             (when (some? max-torque) (set! (.-maxTorque j) max-torque))
;;             (when (some? correction-factor) (set! (.-correctionFactor j) correction-factor))
;;             j)
;;           :mouse
;;           (let [j (MouseJointDef.)]
;;             (when (some? max-force) (set! (.-maxForce j) max-force))
;;             (when (some? frequency) (set! (.-frequencyHz j) frequency))
;;             (when (some? damping) (set! (.-dampingRatio j) damping))
;;             j)
;;           :prismatic
;;           (let [j (PrismaticJointDef.)]
;;             (let [[aa ab] local-anchors]
;;               (when aa (set! (.-localAnchorA j) (as-vec2 aa)))
;;               (when ab (set! (.-localAnchorB j) (as-vec2 ab))))
;;             (when (some? local-axis) (set! (.-localAxisA j) (as-vec2 local-axis)))
;;             (when (some? reference-angle) (set! (.-referenceAngle j) reference-angle))
;;             (when (some? enable-limit?) (set! (.-enableLimit j) enable-limit?))
;;             (when (some? lower-translation) (set! (.-lowerTranslation j) lower-translation))
;;             (when (some? upper-translation) (set! (.-upperTranslation j) upper-translation))
;;             (when (some? enable-motor?) (set! (.-enableMotor j) enable-motor?))
;;             (when (some? max-motor-force) (set! (.-maxMotorForce j) max-motor-force))
;;             (when (some? motor-speed) (set! (.-motorSpeed j) motor-speed))
;;             j)
;;           :pulley
;;           (let [j (PulleyJointDef.)]
;;             (let [[aa ab] local-anchors]
;;               (when aa (set! (.-localAnchorA j) (as-vec2 aa)))
;;               (when ab (set! (.-localAnchorB j) (as-vec2 ab))))
;;             (let [[aa ab] ground-anchors]
;;               (when aa (set! (.-groundAnchorA j) (as-vec2 aa)))
;;               (when ab (set! (.-groundAnchorB j) (as-vec2 ab))))
;;             (let [[la lb] lengths]
;;               (when la (set! (.-lengthA j) la))
;;               (when lb (set! (.-lengthB j) lb)))
;;             (when (some? ratio) (set! (.-ratio j) ratio))
;;             j)
;;           :revolute
;;           (let [j (RevoluteJointDef.)]
;;             (let [[aa ab] local-anchors]
;;               (when aa (set! (.-localAnchorA j) (as-vec2 aa)))
;;               (when ab (set! (.-localAnchorB j) (as-vec2 ab))))
;;             (when (some? reference-angle) (set! (.-referenceAngle j) reference-angle))
;;             (when (some? lower-angle) (set! (.-lowerAngle j) lower-angle))
;;             (when (some? upper-angle) (set! (.-upperAngle j) upper-angle))
;;             (when (some? max-motor-torque) (set! (.-maxMotorTorque j) max-motor-torque))
;;             (when (some? motor-speed) (set! (.-motorSpeed j) motor-speed))
;;             (when (some? enable-limit?) (set! (.-enableLimit j) enable-limit?))
;;             (when (some? enable-motor?) (set! (.-enableMotor j) enable-motor?))
;;             j)
;;           :rope
;;           (let [j (RopeJointDef.)]
;;             (let [[aa ab] local-anchors]
;;               (when aa (set! (.-localAnchorA j) (as-vec2 aa)))
;;               (when ab (set! (.-localAnchorB j) (as-vec2 ab))))
;;             (when (some? max-length) (set! (.-maxLength j) max-length))
;;             j)
;;           :weld
;;           (let [j (WeldJointDef.)]
;;             (let [[aa ab] local-anchors]
;;               (when aa (set! (.-localAnchorA j) (as-vec2 aa)))
;;               (when ab (set! (.-localAnchorB j) (as-vec2 ab))))
;;             (when (some? reference-angle) (set! (.-referenceAngle j) reference-angle))
;;             (when (some? frequency) (set! (.-frequencyHz j) frequency))
;;             (when (some? damping) (set! (.-dampingRatio j) damping))
;;             j)
;;           :wheel
;;           (let [j (WheelJointDef.)]
;;             (let [[aa ab] local-anchors]
;;               (when aa (set! (.-localAnchorA j) (as-vec2 aa)))
;;               (when ab (set! (.-localAnchorB j) (as-vec2 ab))))
;;             (when (some? local-axis) (set! (.-localAxisA j) (as-vec2 local-axis)))
;;             (when (some? enable-motor?) (set! (.-enableMotor j) enable-motor?))
;;             (when (some? max-motor-torque) (set! (.-maxMotorTorque j) max-motor-torque))
;;             (when (some? motor-speed) (set! (.-motorSpeed j) motor-speed))
;;             j))]
;;     (set! (.-userData j) (user-data-def props))
;;     (let [[bodyA bodyB] bodies]
;;       (when bodyA (set! (.-bodyA j) bodyA))
;;       (when bodyB (set! (.-bodyB j) bodyB)))
;;     (set! (.-collideConnected j) (boolean collide-connected?))
;;     j))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;; Build world

;; (defn add-fixture
;;   ([^Body body f]
;;    (cond
;;      (instance? Shape f)
;;      (add-fixture body f 0)
;;      (map? f)
;;      (add-fixture body (fixture-def f))
;;      (instance? FixtureDef f)
;;      (.createFixture body ^FixtureDef f)))
;;   ([^Body body shape density]
;;    (.createFixture body ^Shape shape ^double density)))

;; (defn add-body [^World world props]
;;   (let [body (.createBody world (body-def props))]
;;     (run! (partial add-fixture body) (:fixtures props))
;;     body))

;; (defn indexed [entities]
;;   (into {} (map (juxt (comp :id user-data) identity)) entities))

;; (defn add-joint [^World world props]
;;   (let [id->body (indexed (bodies world))
;;         id->joint (indexed (joints world))
;;         props (-> props
;;                   (update :bodies (partial map id->body))
;;                   (update :joints (partial map id->joint)))]
;;     (.createJoint world (joint-def props))))

;; (defn world [gravity-x gravity-y]
;;   (World. (vec2 gravity-x gravity-y)))

;; (defn populate
;;   ([world bodies]
;;    (populate world bodies nil))
;;   ([world bodies joints]
;;    (run! (partial add-body world) bodies)
;;    (run! (partial add-joint world) joints)
;;    world))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;; Use world

;; (defn step-world
;;   ([world]
;;    (step-world world 1/60))
;;   ([world timestep]
;;    (step-world world timestep 4 2))
;;   ([^World world timestep velocity-iterations position-iterations]
;;    (.step world timestep velocity-iterations position-iterations)))

;; (defn linked-list-seq [x]
;;   (when x
;;     (loop [x x
;;            xs [x]]
;;       (if-let [x (cond (instance? Body x)
;;                        (.getNext ^Body x)
;;                        (instance? Fixture x)
;;                        (.getNext ^Fixture x)
;;                        (instance? Joint x)
;;                        (.getNext ^Joint x))]
;;         (recur x (conj xs x))
;;         xs))))

;; (defn world->screen
;;   ([vec]
;;    (world->screen *camera* vec))
;;   ([^OBBViewportTransform camera vec]
;;    (let [v (Vec2.)]
;;      (.getWorldToScreen camera (as-vec2 vec) v)
;;      v)))

;; (defn screen->world
;;   ([vec]
;;    (screen->world *camera* vec))
;;   ([^OBBViewportTransform camera vec]
;;    (let [v (Vec2.)]
;;      (.getScreenToWorld camera (as-vec2 vec) v)
;;      v)))

;; (defn world-point [^Body body vec]
;;   (.getWorldPoint body (as-vec2 vec)))

;; (defn world-vertices
;;   ([fixture]
;;    (world-vertices (body fixture) fixture))
;;   ([^Body body fixture-or-shape]
;;    (let [vertices (vertices fixture-or-shape)]
;;      (map (partial world-point body) vertices))))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;; Querying

;; (defn raycast-callback ^RayCastCallback [f]
;;   (if (instance? RayCastCallback f)
;;     f
;;     (reify RayCastCallback
;;       (reportFixture [_ fixture point normal fraction]
;;         (f fixture point normal fraction)))))

;; (defn particle-raycast-callback ^ParticleRaycastCallback [f]
;;   (if (instance? ParticleRaycastCallback f)
;;     f
;;     (reify ParticleRaycastCallback
;;       (reportParticle [_ index point normal fraction]
;;         (f index point normal fraction)))))

;; (defn raycast
;;   ([^World world rcb point1 point2]
;;    (.raycast world
;;              (raycast-callback rcb)
;;              (as-vec2 point1)
;;              (as-vec2 point2)))
;;   ([^World world rcb pcb point1 point2]
;;    (.raycast world
;;              (raycast-callback rcb)
;;              (particle-raycast-callback pcb)
;;              (as-vec2 point1)
;;              (as-vec2 point2))))

;; (defn particle-raycast [^World world pcb point1 point2]
;;   (.raycast world
;;             (particle-raycast-callback pcb)
;;             (as-vec2 point1)
;;             (as-vec2 point2)))

;; (defn raycast-seq [^World world point1 point2]
;;   (let [result (volatile! (transient []))]
;;     (raycast world
;;              (fn [fixture _ _ _]
;;                (vswap! result conj! fixture)
;;                1)
;;              point1 point2)
;;     (persistent! @result)))

;; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ;; Properties and Operations

;; (defn ctl! [entity & kvs]
;;   (doseq [[k v] (partition 2 kvs)]
;;     (ctl1! entity k v)))

;; (extend-protocol IProperty
;;   World
;;   (bodies [w]
;;     (linked-list-seq (.getBodyList w)))
;;   (joints [w]
;;     (linked-list-seq (.getJointList w)))
;;   (fixtures [w]
;;     (mapcat fixtures (bodies w)))
;;   (vertices [w]
;;     (mapcat vertices (fixtures w)))

;;   Body
;;   (body [b]
;;     b)
;;   (angle [b]
;;     (.getAngle b))
;;   (position [b]
;;     (.getPosition b))
;;   (fixtures [b]
;;     (linked-list-seq (.getFixtureList b)))
;;   (vertices [b]
;;     (mapcat vertices (fixtures b)))
;;   (user-data [b]
;;     (.getUserData b))

;;   Fixture
;;   (body [f]
;;     (.getBody f))
;;   (angle [f]
;;     (angle (body f)))
;;   (shape [f]
;;     (.getShape f))
;;   (vertices [f]
;;     (vertices (shape f)))
;;   (user-data [f]
;;     (.getUserData f))

;;   PolygonShape
;;   (vertices [s]
;;     (into [] (take (.getVertexCount s) (.getVertices s))))
;;   (centroid [s]
;;     (.m_centroid s))

;;   CircleShape
;;   (vertices [s]
;;     [(.m_p s)])
;;   (centroid [s]
;;     (.m_p s))

;;   EdgeShape
;;   (vertices [s]
;;     [(.m_vertex0 s) (.m_vertex1 s)])
;;   (centroid [s]
;;     (doto ^Vec2 (vec2 0 0)
;;       (.addLocal (.m_vertex0 s))
;;       (.addLocal (.m_vertex1 s))
;;       (.mulLocal 0.5)))

;;   ChainShape
;;   (vertices [s]
;;     (.m_vertices s))

;;   OBBViewportTransform
;;   (position [c]
;;     (.getCenter c))

;;   Joint
;;   (user-data [j]
;;     (.getUserData j)))

;; (extend-protocol IOperations
;;   World
;;   (draw! [w]
;;     (run! draw! (bodies w)))
;;   (ctl1! [w k v]
;;     (case k
;;       :allow-sleep? (.setAllowSleep w v)
;;       :sub-stepping? (.setSubStepping w v)
;;       :gravity (.setGravity w (as-vec2 v))
;;       :particle-max-count (.setParticleMaxCount w v)
;;       :particle-density (.setParticleDensity w v)
;;       :particle-gravity-scale (.setParticleGravityScale w v)
;;       :particle-dampint (.setParticleDamping w v)
;;       :particle-radius (.setParticleRadius w v)))

;;   Body
;;   (draw! [b]
;;     (if-let [draw (:draw (user-data b))]
;;       (draw b)
;;       (run! draw! (fixtures b))))
;;   (ctl1! [b k v]
;;     (case k
;;       :linear-velocity (.setLinearVelocity b (as-vec2 v))
;;       :angular-velocity (.setAngularVelocity b v)
;;       :transform (.setTransform b (as-vec2 (first v)) (second v))
;;       :position (.setTransform b (as-vec2 v) (.getAngle b))
;;       :gravity-scale (.setGravityScale b v)
;;       :allow-sleep? (.setSleepingAllowed b v)
;;       :awake? (.setAwake b v)
;;       :active? (.setActive b v)
;;       :fixed-rotation? (.setFixedRotation b v)
;;       :bullet? (.setBullet b v)))
;;   (alter-user-data! [b f & args]
;;     (.setUserData b (apply f (.getUserData b) args)))
;;   (apply-force! [b force]
;;     (.applyForceToCenter b (as-vec2 force)))
;;   (apply-force! [b force point]
;;     (.applyForce b (as-vec2 force) (as-vec2 point)))
;;   (apply-torque! [b torque]
;;     (.applyTorque b torque))
;;   (apply-impulse! [b impulse point wake?]
;;     (.applyLinearImpulse b (as-vec2 impulse) (as-vec2 point) wake?))
;;   (apply-angular-impulse! [b impulse]
;;     (.applyAngularImpulse b impulse))

;;   OBBViewportTransform
;;   (move-to! [camera center]
;;     (.setCenter camera center)
;;     camera)
;;   (move-by! [camera center]
;;     (let [[^double vx ^double vy] (.getCenter camera)
;;           [^double cx ^double cy] center]
;;       (.setCenter camera (+ vx cx) (+ vy cy))
;;       camera))
;;   (zoom! [camera amount]
;;     (.addLocal (.getTransform camera) (Mat22/createScaleTransform amount)))

;;   Fixture
;;   (draw! [fixture]
;;     (if-let [draw (:draw (user-data fixture))]
;;       (draw fixture)
;;       (draw-shape! (shape fixture) (body fixture))))
;;   (alter-user-data! [fixt f & args]
;;     (.setUserData fixt (apply f (.getUserData fixt) args)))

;;   PolygonShape
;;   (draw-shape! [shape body]
;;     (q/begin-shape)
;;     (let [vs (map (partial world->screen) (world-vertices body shape))]
;;       (doseq [[^double x ^double y] vs]
;;         (q/vertex x y))
;;       (let [[^double x ^double y] (first vs)]
;;         (q/vertex x y)))
;;     (q/end-shape))

;;   CircleShape
;;   (draw-shape! [shape body]
;;     (let [[x y] (world->screen (world-point body (centroid shape)))
;;           radius (double (.-m_radius shape))
;;           matrix (.getTransform *camera*)
;;           [^double scale-x ^double scale-y] (svd/get-scale matrix)]
;;       #_      (prn [x y radius])
;;       (q/push-matrix)
;;       (q/rotate (.getAngle matrix))
;;       (q/ellipse x y (* scale-x radius 2) (* scale-y radius 2))
;;       (q/pop-matrix)))

;;   Joint
;;   (alter-user-data! [j f & args]
;;     (.setUserData j (apply f (.getUserData j) args))))

;; (extend-protocol ICoerce
;;   Vec2
;;   (as-vec2 [v] v)
;;   clojure.lang.Indexed
;;   (as-vec2 [[x y]]
;;     (vec2 x y)))
