(ns cljs-server.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as cookie]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.basic-authentication :as basic]
            [cemerick.drawbridge :as drawbridge]
            [ring.util.response :as response]
            [environ.core :refer [env]]))

(require '[cljs.build.api :as b])
(require '[cljs-server.slurp-goog :as slurp-goog])
(import clojure.lang.RT)
(import java.security.KeyStore)

(def keystore
  (with-open [in (io/input-stream (RT/getResource (RT/baseLoader) "keystore.jks"))]
    (doto (KeyStore/getInstance "JKS")
      (.load in (.toCharArray "password")))))

(defn watch [src]
  (b/watch src
           {:main 'cljs-server.core
            :output-to "out/self_compile.js"
            :output-dir "out"
            :verbose true
            }))

(def cors-headers {
                   :headers {
                             "Access-Control-Allow-Origin" "*"
                             "Access-Control-Allow-Headers" "Content-Type"
                             }})

(defroutes app
  #_(OPTIONS "/" [] (assoc cors-headers
                      :body ""
                      :status 200))
  (GET "/" [root]
       (let [
             root (if root (.replace root "-" "_"))
             response (slurp-goog/slurp-deps root)
             ]
         (if response
           (assoc cors-headers
             :status 200
             :body response)
           (assoc cors-headers
             :status 400
             :body "Invalid Namespace"))))
  (ANY "*" []
       (route/not-found "not found")))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
      (catch Exception e
        {:status 500
         :headers {"Content-Type" "text/html"}
         :body (slurp (io/resource "500.html"))}))))

(defn wrap-app [app]
  ;; TODO: heroku config:add SESSION_SECRET=$RANDOM_16_CHARS
  (let [store (cookie/cookie-store {:key (env :session-secret)})]
    (-> app
        ((if (env :production)
           wrap-error-page
           trace/wrap-stacktrace))
        (site {:session {:store store}}))))

(defn serve
  ([] (serve 7000 true 8000))
  ([port ssl? ssl-port]
   (def server (jetty/run-jetty (wrap-app #'app) {
                                                  :port port
                                                  :join? false
                                                  :ssl? ssl?
                                                  :ssl-port ssl-port
                                                  :keystore keystore
                                                  :key-password "password"
                                                  }))
   (println "done")
   ))

(defn both [port ssl? ssl-port src]
  (serve port ssl? ssl-port)
  (watch src))

(defn -main [& args]
  (println *clojure-version*)
  (serve)
  (watch "src"))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
