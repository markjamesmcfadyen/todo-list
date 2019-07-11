(ns todo-list.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :as p]
            [net.cgrand.enlive-html :as html]
            [ring.util.response :as response]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [not-found]]
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
(def menu
  [:div.menu
   [:a {:href "/"} "Home"]])

(defn layout []
  )

(defn home [request]
  (html5 [:div
          (processed-player-data users)
          [:p "Update goals: "]
          [:form {:method "post" :action "post-update-goal-count"}
           [:input {:type "text" :name "name"}]
           [:input {:type "submit" :value "submit"}]]
          [:div
          [:p "Guess the word: "]
          [:form {:method "post" :action "post-submit"}
           [:input {:type "text" :name "name"}]
           [:input {:type "submit" :value "submit"}]]]]))

(defn success-result []
  (html5
   [:p "success, you guessed correctly"]))


#_(defn get-page
  [request]
  (html5 {:lang "en"}
         menu
         [:body
          [:div#wrap
           [:div.container
            (container request)]]]))

;; Routing
(defroutes routes
  (GET "/" request (home request))
  (POST "/post-submit" request (check-result request))
  (GET "/success" request (success-result)))

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
