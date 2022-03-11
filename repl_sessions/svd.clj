(ns svd
  (:import (org.apache.commons.math3.linear MatrixUtils
                                            RealMatrix
                                            SingularValueDecomposition)
           (org.jbox2d.common Mat22)))

(set! *warn-on-reflection* true) ;; To avoid accidental reflection

#_(defmacro aget2d [a i j]
    `(aget ^"[D" (aget ~a ~i) ~j))

(defmacro aset2d [a i j v]
  `(aset ^"[D" (aget ~a ~i) ~j ~v))

(defn make-matrix [vals]
  (MatrixUtils/createRealMatrix
   (let [^"[[D" a (make-array Double/TYPE (count vals) (count (first vals)))]
     (dotimes [i (count vals)]
       (dotimes [j (count (first vals))]
         (aset2d a i j (get-in vals [i j]))))
     a)))

(make-matrix [[1.0 2.0] [3.0 4.0]])

(def camera
  (let [m (Mat22/mul (Mat22/createRotationalTransform 1.5)
                     ;; => #object[org.jbox2d.common.Mat22 0x6eea72a1 "[0.07078076595527008,-0.9974921571471675]\n[0.9974921571471675,0.07078076595527008]"]
                     (Mat22/createScaleTransform 20)
                     ;; => #object[org.jbox2d.common.Mat22 0x608f850 "[20.0,0.0]\n[0.0,20.0]"]
                     )]
    ))

(def svd (SingularValueDecomposition. camera))

(.getU svd)
;; => #object[org.apache.commons.math3.linear.Array2DRowRealMatrix 0x270a112c "Array2DRowRealMatrix{{-0.9974918976,-0.0707807475},{0.0707807475,-0.9974918976}}"]
(.getV svd)
;; => #object[org.apache.commons.math3.linear.Array2DRowRealMatrix 0x3d80d4d7 "Array2DRowRealMatrix{{0.0,-1.0},{1.0,-0.0}}"]
(.getS svd)
;; => #object[org.apache.commons.math3.linear.Array2DRowRealMatrix 0x75341449 "Array2DRowRealMatrix{{20.000005204,0.0},{0.0,20.000005204}}"]
