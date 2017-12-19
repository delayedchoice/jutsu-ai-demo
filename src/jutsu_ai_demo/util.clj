(ns jutsu-ai-demo.util
 (:import [javax.imageio ImageIO ImageWriter ImageWriteParam IIOImage]) 
 (:require [clojure.data.csv :as csv]
           [clojure.java.io :as io]
           [uncomplicate.neanderthal.native :as neandn]))
   
 

#_(with-open [reader (io/reader "in-file.csv")]
  (doall
    (csv/read-csv reader)))

 

(defn ->one-hot [label]
  (let [src-vec (vec (repeat 10 0))
        test-vec (vec (range 10))
        label-idx (.indexOf test-vec label)]
    (assoc src-vec label-idx 1)))

(defn read-image-bytes
  "Reads a BufferedImage from source, something that can be turned into
  a file with clojure.java.io/file"
  [source]
  (let [image (ImageIO/read (clojure.java.io/resource (apply str (drop 10 (.getPath source)))))]
    (-> image
        (.getData)
        (.getPixels 0 0 28 28  (make-array Integer/TYPE (* 28 28)) )
        )))

(defn get-images [dir] 
  (let [files (file-seq (clojure.java.io/file (str "resources/mnist/" dir)))
        files (filter #(.endsWith (.getName %) ".png") files)
        parnts (map #(.getParent %) files)
        ;labels (map (comp ->one-hot str last) parnts)
        labels (map (comp #(apply str %) ->one-hot #(Integer/parseInt %) str last) parnts)
        files (map read-image-bytes files)
        files (for [f files] (map (comp float (partial * (/ 255)) )  f))
      ;;  files (map neandn/dv files )
 data (shuffle (map (comp reverse conj) files labels ))
        ]
(with-open [writer (io/writer (str "resources/mnist-" dir ".csv"))]
  (csv/write-csv writer data))     ))

