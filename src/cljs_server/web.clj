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
;(import clojure.lang.RT)

(defn watch []
  (b/watch "src"
           {:main 'cljs-server.core
            :output-to "out/self_compile.js"
            :output-dir "out"
            :verbose true
            }))

(defroutes app
  (GET "/" [root]
       {:status 200
        :headers {
                  "Access-Control-Allow-Origin" "*"
                  }
        :body (slurp-goog/slurp-deps root)})
  (GET "/test" []
       {:status 200
        :headers {}
        :body "poos"})
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

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

(defn serve [& [port]]
  (let [port (Integer. (or port (env :port) 7000))]
    (def server (jetty/run-jetty (wrap-app #'app) {:port port :join? false
                                                   :ssl? true
;                                                   :ssl-port port
                                                   :keystore "keys/keystore.jks"
                                                   :key-password "password"
                                                   }))
    (println "done")
    ))

(defn -main [& [port]]
  (serve)
  #_(watch))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
