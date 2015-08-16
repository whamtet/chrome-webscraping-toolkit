(ns cljs-server.core
  (:require crate.core)
  )

(def a (atom 1))
(defn ^:export -main []
  (swap! a inc))
