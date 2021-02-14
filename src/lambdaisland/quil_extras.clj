(ns lambdaisland.quil-extras
  (:require [quil.core :as q]
            [quil.applet :as ap])
  (:import (processing.core PApplet PImage)))

(set! *unchecked-math* :warn-on-boxed)
(set! *warn-on-reflection* true)

;; Type hinted versions of standard quil functions
(defn width ^long [] (q/width))
(defn height ^long [] (q/height))

(defn load-image ^PImage [path]
  (.loadImage (doto (PApplet.) .sketchPath) path))

(defn background-image
  "Set the given image as background image, filling the entire sketch. Will resize
  the sketch/applet if necessary to make the aspect ratio match."
  [file]
  (let [img (load-image file)
        ratio (max (double (/ (width) (.-width img)))
                   (double (/ (height) (.-height img))))
        width (* (.-width img) ratio)
        height (* (.-height img) ratio)]
    (q/resize-sketch width height)
    (q/resize img width height)
    ;; Hack... if the sketch resize hasn't been fully finished yet we'll get an
    ;; error, so retry a few times with small sleep calls in between
    (loop [i 0]
      (when-let [ex (try
                      (q/background-image img)
                      nil
                      (catch java.lang.ArrayIndexOutOfBoundsException e
                        e))]
        (if (< i 5)
          (do
            (Thread/sleep 20)
            (recur (inc i)))
          (throw ex))))
    img))

(defn scale-up-pixels
  "Scale an image up by an integer factor, without any blurring/smoothing"
  [^PImage src ^long factor]
  (let [size-x (.-pixelWidth src)
        size-y (.-pixelHeight src)
        dest (processing.core.PImage. (* factor size-x) (* factor size-y))]
    (set! (.-format dest) (.-format src))
    (doseq [^long x (range size-x)
            ^long y (range size-y)
            :let [pix (int (aget (.-pixels src) (+ x (* size-x y))))]]
      (doseq [^long fx (range factor)
              ^long fy (range factor)]
        (aset (.-pixels dest)
              (+ fx (* x factor)
                 (* size-x factor (+ fy (* y factor))))
              pix)))
    (.updatePixels dest)
    dest))

(defn tile
  "Slice a single tile out of a tile-set image, tile is a vector of [x y width
  height], with width/height optional and defaulting to 1"
  ^PImage [^PImage src ^long tile-size tile]
  (let [[^long x ^long y w h] tile
        w (long (or w 1))
        h (long (or w 1))]
    (.get src
          (* x tile-size)
          (* y tile-size)
          (* w tile-size)
          (* h tile-size))))

(defn tiles [^PImage src ^long tile-size tiles]
  (map (partial tile src tile-size) tiles))

(defn tile-sequence
  "Slice out a number of left-to-right adjacent tiles out of a tileset"
  [^PImage src ^long tile-size tile-spec num]
  (let [^long tile-width (get tile-spec 2 1)]
    (map #(tile src tile-size
                (update tile-spec 0 + (* tile-width ^long %)))
         (range num))))

(defn grid-stroke [i]
  (cond
    (= (mod i 2) 0) (q/stroke 161 165 134)
    :else           (q/stroke 246 206 31)))

(defn grid
  "Draw a line grid over the complete sketch, with column/row numbers. Meant for
  identifying tile coordinates in a tile set."
  [^long size]
  (doseq [^long x (range (Math/ceil (/ (width) size)))]
    (grid-stroke x)
    (q/line (* x size) 0 (* x size) (height))
    (q/text (str x) (* x size) 20))
  (doseq [^long y (range (Math/ceil (/ (height) size)))]
    (grid-stroke y)
    (q/line 0 (* y size) (width) (* y size) )
    (q/text (str y) 1 (* (+ y 0.5) size))))

(defn animate
  "Loops through a sequence of images, drawing one of them on each draw cycle
  based on the current time and fps"
  [tiles ^long fps x y]
  (q/image (nth tiles (mod (long (* (long (q/millis)) 0.001 fps))
                           (count tiles))) x y))
