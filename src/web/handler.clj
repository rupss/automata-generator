(ns web.handler
  (:use compojure.core)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [web.views :as views]
            [web.generation :as gen]
            ))

(defroutes app-routes
  (GET "/" [] (views/main-page))
  (POST "/" [states input] (views/results-page states input))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
