(ns jutsu-ai-demo.seq-classifier-util
 (:require [clojure.data.csv :as csv]
           [clojure.java.io :as io]
           ))
   
 

#_(with-open [reader (io/reader "in-file.csv")]
  (doall
    (csv/read-csv reader)))

 

(defn ->one-hot [label]
  (let [src-vec (vec (repeat 10 0))
        test-vec (vec (range 10))
        label-idx (.indexOf test-vec label)]
    (assoc src-vec label-idx 1)))

(defn load-seq-file2 [file] 
  (with-open [rdr (clojure.java.io/reader file)] 
    (doall 
      (as-> (line-seq rdr)  data
            (map #(clojure.string/split % #" +") data)
            ;(prn (first data))
            (partition 100 data) 
            ;(println (count data))
            (map vector data (iterate inc 0))
            (map #(for [entry (first %)] (cons (second %) entry )) data)
            (mapcat identity data) ;flattens one level
            (shuffle data)
            ;(split-at 500 lines)
            ;(prn (count (first lines)))
            ;(println (second (first data)))
            (let [x (atom 0)] 
                  (for [line data] 
                    (do
                      ;(println line)
                     ; (println (.toString label))
                      (with-open [writer (io/writer (str "resources/rnn-data/train/features/" (swap! x inc) ".csv"))] 
                       (.write writer (clojure.string/join "\n" (rest line))))
                      (spit (str "resources/rnn-data/train/labels/" @x ".csv") (first line)) )) )))))

(defn load-seq-file [file] 
  (with-open [rdr (clojure.java.io/reader file)] 
    (doall 
      (as-> (line-seq rdr)  lines
            (map #(clojure.string/split % #" +") lines)
            ;(prn (first lines))
            (partition 100 lines) 
            (map vector lines (iterate inc 0))
            (map #(for [entry (first %)] (conj entry (second %))) lines)
            (mapcat identity lines)
            ;(prn (count (first lines)))
            (with-open [writer (io/writer (str "resources/seqdata.csv"))]
              ;for [data lines]
                (csv/write-csv writer lines)))))) 
