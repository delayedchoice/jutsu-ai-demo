(ns jutsu-ai-demo.clj-dataset-rnn
 (:require [jutsu.ai.core :as ai]
           [jutsu.ai.ui.core :as ui]
            )
(:import [org.datavec.api.records.reader.impl.csv CSVSequenceRecordReader]
         [org.datavec.api.split NumberedFileInputSplit]
         [org.nd4j.linalg.activations Activation]
         [org.nd4j.linalg.dataset DataSet]
         [org.nd4j.linalg.dataset.api.iterator DataSetIterator]
         [org.nd4j.linalg.dataset.api.preprocessor NormalizerStandardize]
         [org.nd4j.linalg.util ArrayUtil]
         [org.nd4j.linalg.factory Nd4j]
;         [org.deeplearning4j.datasets.datavec SequenceRecordReaderDataSetIterator SequenceRecordReaderDataSetIterator$AlignmentMode ]
         [java.io File]
         [java.util Random]))

(defn gen-data []
  (let [data (take 1000 (repeatedly #(+ 1 (rand-int 10))))
        data (partition 100 1 data )
         ]
    (for [d data] [d (reductions +  d)])))

(->> (take 1 (gen-data))
    (map second))

(defn ->dbls [data]
  (for [l data] (map double l)))

(defn prep-data [data series-length]
  (let [
        number-of-rows (count data)
        _ (prn "data-lenght: " (count data) " series-length: " series-length " num-rows: " number-of-rows)]
    (-> (flatten data)
        (into-array)
        (double-array)
       ; (ArrayUtil/flattenDoubleArray)
        (Nd4j/create (int-array [number-of-rows 1 series-length]) \f)))); 1 because we just have one series as input



;assume the data is partitoned and labeled i.e. [[[1 2 3] [4 5 6] 7]]
(defn sequential-dataset-iterator [base-data batch-size ]
  (let [data (atom base-data)
        ;_ (prn "base-data: " base-data)
        total-record-count (count base-data) 
        feature-count (count (-> base-data first first))
        _ (prn "feature-count: " feature-count)
        dsi (reify org.nd4j.linalg.dataset.api.iterator.DataSetIterator
             (batch [this] batch-size)
             (next [this] (.next this batch-size))
             (resetSupported [this] false)
             (asyncSupported [this] false)
             (setPreProcessor [this pp] (throw (UnsupportedOperationException. "Set: This functionality is not supported")))
             (getPreProcessor [this] (throw (UnsupportedOperationException. "Preprosser: This functionality is not supported")))
              (getLabels [this] nil)
              (totalExamples [this] total-record-count )
             (inputColumns [this] feature-count)
             (totalOutcomes [this] 1)
             (reset [this] (prn "Reset Called"))
             (cursor [this] (- total-record-count (count @data)))
             (numExamples [this] (.totalExamples this))
              (hasNext [this] (> (count @data) 0))
             (next [this n]
               (let [working (take n @data )
                     _ (swap! data #(drop n %))
                     _ (prn "Making features")
                     features (-> (map first working)
                                  ->dbls 
                                  (prep-data feature-count))
                     _ (prn "Making labels")
                     labels (as-> (map second working) xs
                                  (->dbls xs)
                                  (prep-data xs feature-count ))
                     ds (DataSet. features labels)
                    ; _ (prn "ds: " ds )
                     ]
                ds )))
            ]
  dsi))



(def seq-classifier-net-config
	[:seed 123
   :iterations 1
   :learning-rate 0.05
   :optimization-algo :sgd
   :weight-init :xavier
   :updater :rmsprop
   ;:activation :relu
   :regularization true
   :l2 1e-4
   :layers 
   [[:graves-lstm [:n-in 1
                   :n-out 256
                   :activation :tanh
                   :gate-activation-function (Activation/HARDSIGMOID)
                   :drop-out 0.2
                   ]]
    [:graves-lstm [:n-in 256
                   :n-out 256
                   :activation :tanh
                   :gate-activation-function (Activation/HARDSIGMOID)
                   :drop-out 0.2
                    ]]
     [:dense [:n-in 256 :n-out 32 :activation :relu]]
    [:rnn-output  :mcxent  [:n-in 32 :n-out 1 :activation :identity]]]
   :backprop-type :truncated-bptt
  :t-b-p-t-t-forward-length 100
  :t-b-p-t-t-backward-length 100
  :pretrain false
  :backprop true
   ])

(def seq-classifier-net
	(-> seq-classifier-net-config
		  ai/network
		  ui/initialize-with-ui))

(defn seq-classifier-train []
  (let [seq-classifier-training-iterator (sequential-dataset-iterator (take 100 (gen-data)) 5 )
        seq-classifier-testing-iterator (sequential-dataset-iterator (take 100 (gen-data)) 5)  ]
     (-> seq-classifier-net
         (ai/train-net! 3 seq-classifier-training-iterator)
         (ai/save-model "seq-classifier-model.nn"))
     #_(println (ai/evaluate seq-classifier-net seq-classifier-testing-iterator))))
;(seq-classifier-train)
