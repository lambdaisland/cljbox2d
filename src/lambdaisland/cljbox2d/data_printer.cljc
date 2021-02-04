(ns lambdaisland.cljbox2d.data-printer
  (:require [clojure.pprint :as pprint]))

#?(:cljs
   (do
     (defn type-name
       "Best effort to derive a 'type name' in ClojureScript given an object (not a constructor).
  Inherintly flawed because the JavaScript object model does not have classes as
  such. Same logic used in deep-diff2's cljs port of puget."
       [t]
       (let [n (.-name t)]
         (if (empty? n)
           (symbol (pr-str t))
           (symbol n))))

     (defmulti type-name-pprint-dispatch (comp type-name type))

     (defmethod type-name-pprint-dispatch :default [obj]
       (pprint/pprint-simple-default obj))

     (defn- cljs-pprint-recognized? [obj]
       (or (instance? PersistentQueue obj)
           (satisfies? IDeref obj)
           (symbol? obj)
           (seq? obj)
           (map? obj)
           (vector? obj)
           (set? obj)
           (nil? obj)))

     (pprint/set-pprint-dispatch
      (fn [obj]
        (if (cljs-pprint-recognized? obj)
          (pprint/simple-dispatch obj)
          (type-name-pprint-dispatch obj))))))

(defn- use-method
  "Installs a function as a new method of multimethod associated with dispatch-value."
  [multifn dispatch-val func]
  #?(:clj (.addMethod ^clojure.lang.MultiFn multifn dispatch-val func)
     :cljs (-add-method multifn dispatch-val func)))

(defn register-type-printer
  "Register print writer based on a class/type."
  [klass tag print-fn]
  #?(:clj
     (let [print-handler (fn [obj ^java.io.Writer w]
                           (.write w (str "#" tag " " (pr-str (print-fn obj)))))
           pprint-handler (fn [obj]
                            (print (str "#" tag " "))
                            (pprint/write-out (print-fn obj)))]

       (use-method print-method klass print-handler)
       (use-method print-dup klass print-handler)
       (use-method pprint/simple-dispatch klass pprint-handler))

     :cljs
     (let [classname (symbol (type-name klass))
           pprint-handler (fn [obj]
                            (print (str "#" tag " "))
                            (pprint/write-out (print-fn obj)))]
       (extend-type klass
         IPrintWithWriter
         (-pr-writer [obj w opts]
           (-write w (str "#" tag " "))
           (-pr-writer (print-fn obj) w opts)))
       (use-method type-name-pprint-dispatch classname pprint-handler))))
