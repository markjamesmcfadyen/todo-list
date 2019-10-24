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
(def *users
  (atom {1 {:first-name "Lionel"
            :last-name  "Messi"
            :team       "Barca"
            :goals      20
            :id         1}
         2 {:first-name "Christano"
            :last-name  "Ronaldo"
            :team       "Real Madrid"
            :goals      25
            :id         2}}
        ))

(defn add-user [users new]
  (let [new-index (inc (apply max (or (keys users) [0])))]
    (assoc users new-index (assoc new :id new-index))))

(defn remove-user [users user-to-remove]
  (dissoc users user-to-remove))

(defn update-atom-value [users user-to-update-goal value-to-update value]
    (assoc-in users [user-to-update-goal value-to-update] value))


(comment
  @*users
  (def users @*users)

  (add-user users {:first-name "asd"
                     :last-name  "das"
                     :team       "asd"
                     :goals      0})

  (update-atom-value users 1 :goals 520)

  (update-atom-value users 2 :first-name "haha")
  )

(defn processed-player-data [{:keys [users]}]
  (let [headings (keys (get (first users) 1))]
    [:div
     [:table
      [:tr
       (for [heading headings]
         [:td (name heading)])]
      (for [[k v] users]
        [:tr
         (for [heading headings]
           [:td
            (get v heading)])
          ])]])
  )

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

(defmethod container :home [request]
  [:div
   [:h1 "Welcome"]
   (processed-player-data request)])

(defn get-page
  [request]
  (html5 {:lang "en"}
         menu
         [:body
          [:div#wrap
           [:div.container
            (container (assoc request :users @*users))]]]))

;; Routing
(defroutes routes
  (GET "/" [] (get-page {:template :home}))
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
