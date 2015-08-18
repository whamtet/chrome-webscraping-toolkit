(ns cljs-server.core
  (:require
   [ajax.core :refer [GET POST]]
   [crate.core :as crate]

  ))

(enable-console-print!)

(def attributes (js/Function.
                 "element"
                 "  out = []
                 for (var i = 0; i < element.attributes.length; i++) {
                 var x = element.attributes[i]
                 out.push([x.nodeName, x.nodeValue])
                 }
                 return out"))

(defn dom2edn [element]
  (if (.-tagName element)
    (let [
          a (-> element .-tagName .toLowerCase keyword)
          b (into {} (map (fn [[a b]] [(keyword a) b]) (js->clj (attributes element))))
          children (filter identity (map dom2edn (array-seq (.-childNodes element))))
          ]
      (if (not-empty children)
        (vec (list* a b children))
        [a b]))
    (if (.-textContent element)
      (let [
            trimmed (-> element .-textContent .trim)
            ]
        (if (not-empty trimmed) trimmed)))))

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
(println "loaded")
