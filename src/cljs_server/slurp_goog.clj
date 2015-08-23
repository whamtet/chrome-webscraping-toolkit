(ns cljs-server.slurp-goog)
(import clojure.lang.RT)

(defn slurp-cp
  "slurps from classpath"
  [f]
  (slurp (if (.startsWith f "/") (RT/getResource (RT/baseLoader) (.substring f 1)) f)))

(defn slurp-deps-map
  "transforms a deps.js file into edn"
  [f prefix]
  (let [
        s (slurp-cp f)
        lines (filter #(.startsWith % "goog.addDependency")
                      (.split s "\n"))
        raw-deps (map
                  #(-> %
                       (.replace "goog.addDependency" "")
                       (.replace "'" "\"")
                       read-string)
                  lines)
        ]
    (into {}
          (mapcat
           (fn [[location provides requires]]
             (for [provide provides]
               [provide [(str prefix location) requires]]))
           raw-deps))))

(def goog-deps (slurp-deps-map "/deps.js" "/"))

(defn glue-lines
  "glue lines"
  [lines]
  (apply str
         (interpose "\n" lines)))

(defn safe-distinct [s]
  (loop [included #{}
         done []
         todo s]
    (if-let [item (first todo)]
      (if (included item)
        (recur included done (rest todo))
        (recur (conj included item) (conj done item) (rest todo)))
      (list* done))))

(defn deps-seq
  "sequence of deps to be slurped"
  [deps root]
  (distinct
   (reverse
    (map
     (fn [dep]
       [dep (first (deps dep))])
     (tree-seq
      (fn [_] true)
      #(-> % deps second)
      root)))))

(defn deps-seq2
  "deps for root"
  [root]
  (conj
   (deps-seq
    (merge goog-deps (slurp-deps-map "out/cljs_deps.js" "out/goog/"))
    root)
   ["goog.base" "goog/base.js"]))

(defn slurp-dep [[name path]]
  (let [
        lines (map (fn [line]
                     (cond
                      (.startsWith line "goog.require(") ""
                      (.startsWith line "goog.provide(") (format "try{%s}catch(e){}" line)
                      :default line))
                   (.split (slurp-cp path) "\n"))
        ]
    (glue-lines lines)))

(def safe-delete "safe_delete = function(name) {
  if (window[name]) { delete window[name];}}")

(defn predeclare-ns [deps]
  (let [
        deps (distinct (map (fn [[name]] (first (.split name "\\."))) deps))
        ]
    (map #(format "safe_delete('%s');" %) deps)))

(defn slurp-deps [root]
  (let [deps (deps-seq2 root)]
    (glue-lines (conj
                 (concat
                  (predeclare-ns deps)
                  (map slurp-dep deps))
                 safe-delete))))
