(defproject
  qcjs "0.1.0-SNAPSHOT"

  :description "Clojurescript wrapper for qc.js"

  :dev-dependencies
  [[lein-clojurescript "1.1.1-SNAPSHOT"]]

  :checksum-deps true

  :exclusions
  [org.apache.ant/ant
   com.google.code.findbugs/jsr305
   junit/junit]

  :extra-classpath-dirs ["macros"]

  :cljs-test-cmd ["phantomjs" "test.js"]
  :cljs-optimizations :simple
  :cljs-externs ["externs/qc.js"]
  :cljs-output-to "out/all.js"
  :cljs-output-dir "out"
  :cljs-pretty-print true)
