(ns demo.cljbox2d.testbed
  (:require [lambdaisland.cljbox2d :as b])
  (:import (org.jbox2d.testbed.framework.jogl JoglPanel JoglDebugDraw)
           java.awt.BorderLayout
           java.awt.Component
           javax.swing.JFrame
           javax.swing.JOptionPane
           javax.swing.JScrollPane
           javax.swing.SwingUtilities
           org.jbox2d.testbed.framework.TestList
           org.jbox2d.testbed.framework.TestbedTest
           org.jbox2d.testbed.framework.TestbedController
           org.jbox2d.testbed.framework.TestbedSettings
           org.jbox2d.testbed.framework.AbstractTestbedController$MouseBehavior
           org.jbox2d.testbed.framework.AbstractTestbedController$UpdateBehavior
           org.jbox2d.testbed.framework.TestbedErrorHandler
           org.jbox2d.testbed.framework.TestbedModel
           org.jbox2d.testbed.framework.j2d.TestbedSidePanel))

;; This simply replicates JoglTestbedMain, so you can run the test cases that
;; come with jbox2d
(defn testbed-orig []
  (let [model (TestbedModel.)
        controller (TestbedController.
                    model
                    AbstractTestbedController$UpdateBehavior/UPDATE_IGNORED
                    AbstractTestbedController$MouseBehavior/FORCE_Y_FLIP
                    (reify TestbedErrorHandler
                      (serializationError [this e msg])))
        panel (JoglPanel. model controller)
        testbed-panel (doto (JFrame.)
                        (.setTitle "JBox2D Testbed")
                        (.setLayout (BorderLayout.)))
        side-panel (TestbedSidePanel. model controller)]
    (doto model
      (.setDebugDraw (JoglDebugDraw. panel))
      (.setPanel panel))
    (TestList/populateModel model)
    (set! (.. model getSettings (getSetting TestbedSettings/DrawWireframe) -enabled) false)

    (doto testbed-panel
      (.add panel "Center")
      (.add (JScrollPane. side-panel) "East")
      .pack
      (.setVisible true))

    (SwingUtilities/invokeLater (fn []
                                  (doto controller
                                    (.playTest 0)
                                    .start)))))

(def testbed-model (doto (TestbedModel.)
                     TestList/populateModel))

(defn launch []
  (let [model testbed-model
        controller (TestbedController.
                    model
                    AbstractTestbedController$UpdateBehavior/UPDATE_IGNORED
                    AbstractTestbedController$MouseBehavior/FORCE_Y_FLIP
                    (reify TestbedErrorHandler
                      (serializationError [this e msg])))
        panel (JoglPanel. model controller)
        testbed-panel (doto (JFrame.)
                        (.setTitle "JBox2D Testbed")
                        (.setLayout (BorderLayout.)))
        side-panel (TestbedSidePanel. model controller)]
    (doto model
      (.setDebugDraw (JoglDebugDraw. panel))
      (.setPanel panel))

    (set! (.. model getSettings (getSetting TestbedSettings/DrawWireframe) -enabled) false)

    (doto testbed-panel
      (.add panel "Center")
      (.add (JScrollPane. side-panel) "East")
      .pack
      (.setVisible true))

    (SwingUtilities/invokeLater (fn []
                                  (doto controller
                                    (.playTest 0)
                                    .start)))))


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

(defn random [i a]
  (+ i (rand (- a i))))

(defn random-body []
  {:position [(random 1 11) (random -2 7)]
   :type :dynamic
   :fixtures [{:shape
               (rand-nth
                [[:circle (random 0.2 0.6)]
                 [:rect (random 0.4 1.2) (random 0.4 1.2)]])
               :restitution 0.1
               :density 1
               :friction 3}]})

(defn testbed-test [world]
  (proxy [TestbedTest] []
    (initTest [_]
      (set! (.-m_world this) world))
    (getTestName []
      (str `testbed-test))))


(launch)

(.addTest testbed-model
          (testbed-test (-> (b/world 0 -9)
                            (b/populate walls)
                            (b/populate (take 50 (repeatedly random-body))))))

(.addTest testbed-model
          (testbed-test (-> (b/world 0 -10)
                            (b/populate [{:fixtures [{:shape [:edge [-40 0] [40 0]]}]}])
                            (b/populate
                             (for [i (range 20)
                                   j (range i 20)]
                               {:type :dynamic
                                :position [(+ -7 (* i 0.5625) (* j 1.125)) (+ 0.75 (* j 1.25))]
                                :fixtures [{:shape [:rect 0.5 0.5]}]})))))
