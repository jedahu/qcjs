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

(defgenerator monotonic [from]
  (let [n (atom (dec from))]
    {:func (fn [size] (swap! n inc))}))

(defgenerator gensyms []
  {:func (fn [size] (gensym "qcjs"))})

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
                                (max lsize min-size)
                                lsize))]
                 (qc/generateValue gen size)))))
   :shrink (or shrink vec-shrink-one)})

;<?
(qc/qcheck "vectors min-size" [(+integers) (+integers)]
  (fn [c n min]
    (let [res (qc/generateValue (vectors (nils) nil min) n)]
      (.noteArg c res)
      (.assert c (<= min (count res))))))

(qc/qcheck "vec-shrink-one" [(vectors (integers))]
  (fn [c v]
    (let [res (vec-shrink-one 0 v)]
      (.noteArg c res)
      (if (= 0 (count v))
        (.assert c (= [] (vec res)))
        (.assert c (apply = (dec (count v)) (map count (vec res))))))))
;?>

(defgenerator vectors-of-size [gens & [shrink]]
  {:func (fn [size] (vec (map #(qc/generateValue % size) gens)))
   :shrink shrink})

;<?
(qc/qcheck "vectors-of-size size" [(+integers) (+integers)]
  (fn [c n s]
    (let [res (qc/generateValue (vectors-of-size (repeat n (integers))) s)]
      (.noteArg c res)
      (.assert c (= n (count res))))))
;?>


(defn non-empty-vectors [gen & [shrink]]
  (vectors gen shrink 1))

;<?
(qc/qcheck "non-empty-vectors are not empty" [(+integers)]
  (fn [c s]
    (let [res (qc/generateValue (non-empty-vectors (integers)) s)]
      (.noteArg c res)
      (.assert c (seq res)))))
;?>

(defshrink map-shrink-one [size m]
  (cond
    (or (not m) (empty? m)) []
    (= 1 (count m)) [{}]
    :else
    (vec
      (for [k (keys m)]
        (dissoc m k)))))

(defgenerator maps [key-gen val-gen & [shrink & [min-size]]]
  {:func (fn [size]
           (let [msize (qc/getPositiveInteger size)]
             (apply hash-map
                    (apply concat
                      (for [i (range (if min-size
                                       (max msize min-size)
                                       msize))]
                        [(qc/generateValue key-gen size)
                         (qc/generateValue val-gen size)])))))
   :shrink (or shrink map-shrink-one)})

;<?
(qc/qcheck "maps min-size" [(+integers) (+integers)]
  (fn [c min s]
    (let [res (qc/generateValue (maps (monotonic) (strings) nil min) s)]
      (.noteArg c res)
      (.assert c (<= min (count res))))))

(qc/qcheck "map-shrink-one" [(maps (gensyms) (integers))]
  (fn [c m]
    (let [res (vec (map-shrink-one 0 m))]
      (.noteArg c res)
      (if (= 0 (count m))
        (.assert c (= [] res))
        (do
          (.assert c (apply = (dec (count m)) (map count res)))
          (.assert c (= (count res) (count (set res)))))))))
;?>

(defn non-empty-maps [key-gen val-gen & [shrink]]
  (maps gen shrink 1))
