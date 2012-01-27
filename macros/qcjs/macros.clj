(ns qcjs.macros)

(defmacro defshrink [name [size x] & body]
  `(defn ~name [~size ~x]
     (. (do ~@body) -array)))

(defmacro defgenerator [name args & body]
  `(defn ~name ~args
     (let [map# (do ~@body)]
       (. {"func" (:func map#) "shrink" (:shrink map#)} -strobj))))
