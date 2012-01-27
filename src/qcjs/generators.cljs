(ns qcjs.generators
  (:require
    [qcjs :as qc])
  (:use-macros
    [qcjs.macros :only (defshrink defgenerator)]))

(def arrays (. qc/generator -arrays))
(def arrays-of-size (. qc/generator -arraysOfSize))
(def non-empty-arrays (. qc/generator -nonEmptyArrays))
(def booleans (. qc/generator -booleans))
(def choose-generator (. qc/generator -chooseGenerator))
(def choose-value (. qc/generator -chooseValue))
(def dates (. qc/generator -dates))
(def mod (. qc/generator -mod))
(def nil-or (. qc/generator -nullOr))
(def nils (. qc/generator -nulls))
(def floats (.. qc/generator -number -floats))
(def integers (.. qc/generator -number -integers))
(def +integers (.. qc/generator -number -positiveIntegers))
(def between (.. qc/generator -number -range))
(def characters (.. qc/generator -string -characters))
(def non-empty-strings (.. qc/generator -string -nonEmptys))
(def strings (.. qc/generator -string -strings))

(defshrink vec-shrink-one [size v]
  (cond
    (or (not v) (empty? v)) []
    (= 1 (count v)) [[]]
    :else
    (vec
      (for [i (range (count v))]
        (vec
          (for [j (range (count v))
                :when (not= i j)]
            (nth v j)))))))

(defgenerator vectors [gen & [shrink & [min-size]]]
  {:func (fn [size]
           (let [lsize (qc/getPositiveInteger size)]
             (vec
               (for [i (range (if min-size
                                (.max js/Math lsize min-size)
                                lsize))]
                 (qc/generateValue gen size)))))
   :shrink (or shrink vec-shrink-one)})

;<?
(qc/qcheck "vectors min-size" [(integers) (integers)]
  (fn [c n min]
    (let [res (qc/generateValue (vectors (strings) nil min) n)]
      (.noteArg c res)
      (.assert c (<= min (count res))))))
;?>
