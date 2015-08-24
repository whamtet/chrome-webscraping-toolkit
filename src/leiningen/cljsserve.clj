(ns leiningen.cljsserve)
(require 'leiningen.core.eval)

(defn parse-args [args]
  (let [
        kw? #(.startsWith % ":")
        nkw? #(not (.startsWith % ":"))
        valid-args (drop-while nkw? args)
        read-kw #(keyword (.substring % 1))
        ]
    (if (empty? valid-args)
      {}
      (loop [todo (rest valid-args)
             done {}
             last-k (read-kw (first valid-args))
             values []]
        (if-let [arg (first todo)]
          (if (kw? arg)
            (let [value (condp = (count values)
                          0 true
                          1 (first values)
                          values)]
              (recur (rest todo) (assoc done last-k value) (read-kw arg) []))
            (recur (rest todo) done last-k (conj values arg)))
          (let [value (condp = (count values)
                        0 true
                        1 (first values)
                        values)]
            (assoc done last-k value)))))))

(defn print-though [x]
  (prn x) x)

(defn update-deps [deps]
  (conj
   (for [[sym ver] deps
         :when (not (#{"clojure" "cljs-server" "clojurescript"} (name sym)))]
     [sym ver])
   '[cljs-server "0.1.0-SNAPSHOT"]))

(defn cljsserve
  "Compiles your clojurescript and serves it as javascript.
  Optional args
  :port (default 7000)
  :ssl? (default false)
  :ssl-port (default 8000)
  :src clojurescript source location (default \"src\")
  "
  [project & args]
  (let [
        {:keys [port ssl? ssl-port src]} (parse-args args)
        port (if port (Integer/parseInt port) 7000)
        ssl-port (if ssl-port (Integer/parseInt ssl-port) 8000)
        src (or src "src")
        project (update-in project [:dependencies] update-deps)
        ]
    (leiningen.core.eval/eval-in-project
     project
     `(cljs-server.web/both ~port ~ssl? ~ssl-port ~src)
     '(require 'cljs-server.web))))
