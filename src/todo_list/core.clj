(ns todo-list.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :as p]
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
(def users {:username "sally"
            :profile {:name "Sally Clojurian"
                      :address {:city "Austin" :state "TX" :postal-code "7130"}}})

;; Helpers
(defn get-user-address-info [users]
  (let [address         (get-in users [:profile :address])
        value-processed (map (fn [[key value]] value) address)
        key-processed   (map (fn [[key value]] key) address)]
    [:div
     [:ul.list
      [:p (str address)]
      [:p value-processed]
      [:p key-processed]
      (for [values value-processed]
        [:li {:class (seq key-processed)}
         values])]]))

(defn get-user-name [users]
  (get-in users [:profile :name]))

(defn input-control [type id name & [value]]
  [:div.form-group
   (list
     (label id name)
     (type {:class "form-control"} id value))])


;; Template
(defn head [title]
  [:head
   [:title title]
   (include-css "/css/todo.css")])

(def menu
  [:div.menu
   [:a {:href "/"} "Home"]
   [:a {:href "/users"} "Users"]
   [:a {:href "/signup"} "Signup"]])

(defmulti container :template :default :not-found)

; (defmethod container [request])

(defmethod container :home [{:keys [request]}]
  [:div
   [:h1 "Welcome"]
   [:p "what you wana do"]
   [:p [:a {:href "/get-form"} "Submit a get request"]]
   [:p [:a {:href "/post-form"} "Submit a post request"]]])

(defmethod container :get-form [{:keys [request]}]
  [:div
   [:h1 "Get form"]
   [:p "Submit a message with GET"]
   [:form {:method "get" :action "get-submit"}
    [:input {:type "text" :name "name"}]
    [:input {:type "submit" :value "submit"}]]])

(defmethod container :post-form [{:keys [request]}]
  [:div
   [:h1 "post form"]
   [:p "Submit a message with post"]
   [:form {:method "post" :action "post-submit"}
    [:input {:type "text" :name "name"}]
    [:input {:type "submit" :value "submit"}]]])

 (defmethod container :display-result [{:keys [request]}]
   (let [{:keys [params uri]} request
         param-name (get params "name")
         request-type (if (= uri "/post-submit") "POST" "GET")]
     [:div
      [:h1 (str "hello" param-name)]
      [:p (str "submitted via a " request-type " request")]]))

; (defmethod container :signup [context]
;   [:div.form-group
;   (form-to [:post "/signup"]
;            (input-control text-field "name" "Name" "name")
;            (submit-button {:class "btn btn-success"} "Create account"))])
;
; (defmethod container :users [context]
;   [:div
;    [:h1 (str "Username: " (get-user-name users))]
;    [:p "Address Info:"]
;    (get-user-address-info users)])

(defn get-page
  [{:keys [title request]
    :or {title "Bluth company"}
    :as context}]
  (html5 {:lang "en"}
         (head title)
         menu
         [:body
          [:div#wrap
           [:div.container
            (container context)]]]))

;; Routing
(defroutes routes
  (GET "/" [] (get-page {:template :home}))
  (GET "/get-form" [] (get-page {:template :get-form}))
  (GET "/post-form" [] (get-page {:template :post-form}))
  (GET "/get-submit" [] (get-page {:template :display-result}))
  (POST "/post-submit" [] (get-page {:template :display-result}))

  (GET "/signup" [] (get-page {:template :signup}))
  (GET "/users" [] (get-page {:title        "User List"
                              :template     :users}))
  (route/resources "/")
  (route/not-found "Page not found"))

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
