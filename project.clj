(defproject life "0.1.0"
  :description "Conway's Game of Life"
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :main ^:skip-aot life.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
