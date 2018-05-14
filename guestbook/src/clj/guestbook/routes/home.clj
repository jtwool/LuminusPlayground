(ns guestbook.routes.home
  (:require [guestbook.layout :as layout]
            [guestbook.db.core :as db]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [guestbook.db.core :as db]
            [ring.util.response :refer [redirect]]
            [struct.core :as st]
            ))

(defn home-page [{:keys [flash]}]
  (layout/render
    "home.html"
    (merge {:messages (db/get-messages)}
           (select-keys flash [:name :message :errors]))))
   
(defn about-page []
  (layout/render "about.html"))

(def message-schema
  [[:name
    st/required
    st/string]
   [:message
    st/required
    st/string
    {:message "Message must contain at least 10 characters"
     :validate #(>= (count %) 10)}]])

(defn validate-message [params]
  (first (st/validate params message-schema)))

(defn save-message! [{:keys [params]}]
  (if-let [errors (validate-message params)]
    (-> (response/found "/")
        (assoc :flash (assoc params :errors errors)))
  (do
    (db/save-mesage!
      (assoc params :timestamp (java.util.Date.)))
    (response/found "/"))))

(defroutes home-routes
  (GET "/" request (home-page request))
  ; Note: overloaded route handling--v. cool!
  (POST "/" request (save-message! request))
  (GET "/about" [] (about-page)))

