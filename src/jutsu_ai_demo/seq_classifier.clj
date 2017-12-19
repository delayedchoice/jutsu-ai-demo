(ns jutsu-ai-demo.seq-classifier
 (:require [jutsu.ai.core :as ai]
        ;    [jutsu.ai.ui.core :as ui]
            ) 
(:import [org.datavec.api.records.reader.impl.csv CSVSequenceRecordReader]
         [org.datavec.api.split NumberedFileInputSplit]  
         [org.nd4j.linalg.dataset.api.iterator DataSetIterator]
         [org.nd4j.linalg.dataset.api.preprocessor NormalizerStandardize]
         [org.deeplearning4j.datasets.datavec SequenceRecordReaderDataSetIterator SequenceRecordReaderDataSetIterator$AlignmentMode ] 
         [java.io File]
         [java.util Random]))

(defn seq-data-reader [dir fst lst]
  (let [srr-features (CSVSequenceRecordReader.)
        _ (.initialize srr-features (NumberedFileInputSplit. (str dir "/features/%d.csv") fst lst))
        srr-labels (CSVSequenceRecordReader.)
       _ (.initialize srr-labels (NumberedFileInputSplit. (str dir "/labels/%d.csv") fst lst))
        dsi (SequenceRecordReaderDataSetIterator. srr-features srr-labels 10 6 false SequenceRecordReaderDataSetIterator$AlignmentMode/ALIGN_END)
        normalizer (NormalizerStandardize.) 
        _ (.fit normalizer dsi ) 
        _ (.setPreProcessor dsi normalizer) ] 
    dsi))


(def seq-classifier-net-config
	[:seed 123
   :optimization-algo :sgd
   :weight-init :xavier
   :updater :nesterovs
   :learning-rate 0.005
   :activation :relu
   :iterations 1
   :regularization true
   :l2 1e-4
   :layers 
		[[:graves-lstm [:n-in 1 :n-out 10 :activation :tanh]]
	 	 [:rnn-output  :mcxent  [:n-in 10 :n-out 6 :activation :softmax]]] ])

(def seq-classifier-net
	(-> seq-classifier-net-config
		  ai/network
		  ai/initialize-net))

(defn seq-classifier-train []
  (let [seq-classifier-training-iterator (seq-data-reader "resources/rnn-data/train" 1 450)
        seq-classifier-testing-iterator (seq-data-reader "resources/rnn-data/train" 451 600)  ]
     (-> seq-classifier-net
         (ai/train-net! 3 seq-classifier-training-iterator)
         (ai/save-model "seq-classifier-model.nn"))
     #_(println (ai/evaluate seq-classifier-net seq-classifier-testing-iterator))))
#_(seq-classifier-train)
