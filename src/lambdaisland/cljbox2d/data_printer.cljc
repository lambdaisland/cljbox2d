(ns lambdaisland.cljbox2d.data-printer
  (:require [lambdaisland.data-printers :as dp]))

(defn register-print [type tag to-edn]
  (dp/register-print type tag to-edn)
  (dp/register-pprint type tag to-edn)

  ;; `alter-var-root!` this function before loading cljbox2d if you want
  ;; printers to be registered for other printing backends

  ;; (dp-puget/register-puget type tag to-edn)
  ;; (dp-ddiff/register-deep-diff type tag to-edn)
  ;; (dp-ddiff2/register-deep-diff2 type tag to-edn)
  ;; (dp-transit/register-write-handler type tag to-edn)
  )
