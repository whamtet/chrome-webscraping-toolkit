(ns cljs-server.colliers
  (:require [cljs-server.core :as core]
            clojure.set
            [crate.core :as crate]
            ))

(core/my-get "colliers" #(def links (atom (set %))))

(defn add-urls [urls]
  (swap! links clojure.set/union (set urls))
  (core/my-set "colliers" @links)
  (prn (count @links))
  )

(defn add-urls2 []
  (add-urls (map #(.-href %) (array-seq (js/$ "h5 > a")))))


(def button (crate/html [:input {:type "button" :value "Save" :onclick "cljs_server.colliers.add_urls2()"}]))

;(-> "h1" js/$ array-seq first .-parentElement (.appendChild button))
