(ns cljs-server.jll
  (:require
   [ajax.core :refer [GET POST]]
   [crate.core :as crate]
  ))
(enable-console-print!)
(defn send-to-octant [links]
  (prn links)
  (POST "http://localhost:5000/jll" {:params {:links links}
                                     :handler #(-> % pr-str js/alert)
                                     }))

(defn select-links []
  (map #(-> % dom2edn second) (array-seq (js/$ "h3 > a"))))

(set! js/s #(send-to-octant (select-links)))

(def button (crate/html [:input {:type "button" :onclick "s()" :value "save"}]))

(.append (js/$ "#defaultChannelDiv > div > div") button)
