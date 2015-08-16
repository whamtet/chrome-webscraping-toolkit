(ns cljs-server.slurp-goog)

(def lines
  (filter #(.startsWith % "goog")
          (.split
           (slurp "out/goog/deps.js") "\n")))

(def raw-deps (map
               #(-> %
                    (.replace "goog.addDependency" "")
                    (.replace "'" "\"")
                    read-string) lines))

(def deps (into {}
                (mapcat
                 (fn [[location provides requires]]
                   (for [provide provides]
                     [provide [location requires]]))
                 raw-deps)))

(defn glue-lines [lines]
  (apply str
         (interpose "\n" lines)))

(defn deps-seq [root]
  (reverse
   (distinct
    (map
     (fn [dep]
       [dep (first (deps dep))])
     (tree-seq
      (fn [_] true)
      #(-> % deps second)
      root)))))

(defn slurp-dep [[name path]]
  (let [
        lines (.split (slurp (str "goog/" path)) "\n")
        lines (remove #(or (.startsWith % "goog.provide")
                           (.startsWith % "goog.require"))
                     lines)
        lines (conj lines
              (format "declare_ns('%s')" name))
        ]
    (glue-lines lines)))

(def declare-ns "declare_ns = function(name) {
  var breakdown = name.split('.')
  var parent = window
  for (var i = 0; i < breakdown.length; i++) {
  if (!parent[breakdown[i]]) {
  parent[breakdown[i]] = {}
  }
  parent = parent[breakdown[i]]
  }};")

(defn slurp-deps [root]
  (glue-lines (conj
               (map slurp-dep (deps-seq root))
               declare-ns)))

;(spit "test.js" (str declare-ns (slurp-deps "goog.dom")))
