(ns lambdaisland.data-printer
  (:require [clojure.pprint :as pprint]))

(defn- use-method
  "Installs a function as a new method of multimethod associated with dispatch-value."
  [multifn dispatch-val func]
  (.addMethod ^clojure.lang.MultiFn multifn dispatch-val func))

(defn register-type-printer
  "Register print writer based on a class/type."
  [klass tag print-fn]
  (let [print-handler (fn [obj ^java.io.Writer w]
                        (.write w (str "#" tag " " (pr-str (print-fn obj)))))
        pprint-handler (fn [obj]
                         (print (str "#" tag " "))
                         (pprint/write-out (print-fn obj)))]

    (use-method print-method klass print-handler)
    (use-method print-dup klass print-handler)
    (use-method pprint/simple-dispatch klass pprint-handler)))

