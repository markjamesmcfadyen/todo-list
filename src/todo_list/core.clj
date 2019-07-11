(ns todo-list.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :as p]
            [ring.util.response :as response]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [hiccup.core :refer :all]
            [hiccup.page :refer :all]
            [hiccup.form :refer :all]
            [noir.response :as resp]
            [todo-list.validators.user-validator :as v]
            [ring.handler.dump :refer [handle-dump]]))

;; Data
(def users {:username "lionelmessi"
            :profile {:first-name "Lionel"
                      :last-name "Messi"
                      :stats {:goals 20 :assists 19 :yellow-cards 0}
                      :address {:city "Barcalona" :state "Barcalona" :postal-code "7130"}}})


;;Update Goals
(let [stats (get-in users [:profile :stats])]
  (update stats :goals inc))

;; Helpers
(defn processed-player-data [users]
  (let [full-name (str (get-in users [:profile :first-name])
                       " "
                       (get-in users [:profile :last-name]))
        stats (get-in users [:profile :stats])
        stats-values (map (fn [[key value]] value) stats)
        stats-headings (map (fn [[key value]] key) stats)]
    [:div
     [:table
      [:tr
       [:th "Full name:"]
       (for [values stats-headings]
        [:th values])]
      [:tr
       [:td (str full-name)]
       (for [keys stats-values]
         [:td keys])]]]))

(defn check-result [request]
  (let [{:keys [params]} request
        param-name (get params "name")]
    (if (re-matches #"mark" param-name)
      (response/redirect "/success")
      (response/redirect "/"))))

;; Template
(defn head []
  [:head
   (include-css "/css/todo.css")])

(def menu
  [:div.menu
   [:a {:href "/"} "Home"]
   [:a {:href "/users"} "Users"]
   [:a {:href "/signup"} "Signup"]])

(defmulti container :template)

(defmethod container :home [{:keys [request]}]
  [:div
   [:h1 "Welcome"]

   (processed-player-data users)


   [:p "Update goals: "]
   [:form {:method "post" :action "update-goal-count"}
    [:input {:type "submit" :value "submit"}]]


   [:p "guess word"]
   [:form {:method "post" :action "post-submit"}
    [:input {:type "text" :name "name"}]
    [:input {:type "submit" :value "submit"}]]
   [:p [:a {:href "/post-form"} "Submit a post request"]]])

(defmethod container :success [{:keys [request]}]
  [:div
   [:h1 "grats"]])

(defn get-page
  [request]
  (html5 {:lang "en"}
         menu
         [:body
          [:div#wrap
           [:div.container
            (container request)]]]))

;; Routing
(defroutes routes
  (GET "/" [] (get-page {:template :home}))
  (GET "/success" [] (get-page {:template :success}))
  (POST "/post-submit" request (check-result request))
  ;(POST "/update-goal-count" request (increment-goal-count request))
  (route/resources "/"))

(def app
  (-> routes
      p/wrap-params))

(defn -dev-main
  [port-number]
  (jetty/run-jetty (wrap-reload #'app)
                   {:port (Integer. port-number)}))

(defn -main
  [port-number]
  (jetty/run-jetty app
                   {:port (Integer. port-number)}))
