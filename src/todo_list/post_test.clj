(ns todo-list.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.util.response :as response]
            [ring.middleware.params :as p]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [not-found]]
            [hiccup.core :refer :all]
            [hiccup.page :refer :all]
            [hiccup.form :refer :all]))

(defn main [request]
   "<div>
      <h1>Hello Web Page with Routing!</h1>
      <p>What would you like to do?</p>
      <p><a href='./get-form.html'>Submit a GET request</a></p>
      <p><a href='./post-form.html'>Submit a POST request</a></p>
    </div>")

(defn get-form [request]
   "<div>
      <h1>Hello GET Form!</h1>
      <p>Submit a message with GET</p>
      <form method=\"get\" action=\"get-submit\">
       <input type=\"text\" name=\"name\" />
       <input type=\"submit\" value\"submit\" />
      </form>
      <p><a href='..'>Return to main page</p>
    </div>")

(defn post-form [request]
   "<div>
      <h1>Hello POST Form!</h1>
      <p>Submit a message with POST</p>
      <form method=\"post\" action=\"post-submit\">
       <input type=\"text\" name=\"name\" />
       <input type=\"submit\" value\"submit\" />
      </form>
      <p><a href='..'>Return to main page</p>
    </div>")

(defn display-result [request]
  (let [{:keys [params uri]} request
        param-name (get params "name")
        request-type (if (= uri "/post-submit") "POST" "GET")]
    (str
     "<div>
        <h1>Hello " param-name "!</h1>
        <p>Submitted via a " request-type " request.</p>
        <p><a href='..'>Return to main page</p>
      </div>")))

;; Routing
(defroutes routes
  (GET "/" request (main request))
  (GET "/get-form.html" request (get-form request))
  (GET "/post-form.html" request (post-form request))
  (GET "/get-submit" request (display-result request))
  (POST "/post-submit" request (display-result request)))

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
