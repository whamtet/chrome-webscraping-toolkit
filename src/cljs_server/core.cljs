(ns cljs-server.core
;  (:require crate.core)
  )

(enable-console-print!)
(js/alert "hi")

(defn ^:export require-code [root]
  (let [
        url (str "http://localhost:5000/?root=cljs_server.core")
        ]
    (js/$.get url #(-> % #_fix-js js/eval))))

(defn ^:export at []
  (println "hi")
  #_(let [s (js/document.createElement "script")]
    (set! (.-src s) "http://localhost:5000/?root=cljs_server.core")
    (js/document.head.appendChild s)))

(defn ^:export reload [server root]
  (let [
        server (or server "http://localhost:5000/")
        root (or root "cljs_server.core")
        s (js/document.createElement "script")
        ]
    (set! (.-src s) (str server "?root=" root))
    (js/document.head.appendChild s)))

(def a (atom 1))
(defn ^:export -main []
  (swap! a inc))
