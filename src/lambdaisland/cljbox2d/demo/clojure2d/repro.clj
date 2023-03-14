(ns lambdaisland.cljbox2d.demo.clojure2d.repro
  (:require
    [lambdaisland.cljbox2d :as b]
    [lambdaisland.cljbox2d :as b]))



(def world (b/world 0 0))

(b/add-body
  world
  {:position [0 0]
   :type :dynamic
   :fixtures [{:shape [:circle [20 20] 20]}]})
