(ns lambdaisland.cljbox2d.svd
  "Singular Value Transform"
  (:import (org.apache.commons.math3.linear MatrixUtils
                                            RealMatrix
                                            SingularValueDecomposition)
           (org.jbox2d.common Mat22)))

(defmacro aset2d [a i j v]
  `(aset ^"[D" (aget ~a ~i) ~j ~v))

(defn mat22->real [^Mat22 m]
  (MatrixUtils/createRealMatrix
   (let [^"[[D" a (make-array Double/TYPE 2 2)]
     (aset2d a 0 0 (.-x (.-ex m)))
     (aset2d a 0 1 (.-x (.-ey m)))
     (aset2d a 1 0 (.-y (.-ex m)))
     (aset2d a 1 1 (.-y (.-ey m)))
     a)))

(defn get-scale
  "Returns the scale factor along X and Y axis"
  [^Mat22 m]
  (let [svd (SingularValueDecomposition. (mat22->real m))
        sigma (.getS svd)]
    [(.getEntry sigma 0 0) (.getEntry sigma 1 1)]))

(comment
  (let [m (Mat22/mul (Mat22/createRotationalTransform 1.5)
                     ;; => #object[org.jbox2d.common.Mat22 0x6eea72a1 "[0.07078076595527008,-0.9974921571471675]\n[0.9974921571471675,0.07078076595527008]"]
                     (Mat22/createScaleTransform 20)
                     ;; => #object[org.jbox2d.common.Mat22 0x608f850 "[20.0,0.0]\n[0.0,20.0]"]
                     )]
    (get-scale m))
  ;; => [20.00000520399257 20.000005203992565]
)
