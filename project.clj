(defproject todo-list "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [ring "1.7.1"]
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]
                 [com.novemberain/validateur "2.6.0"]
                 [lib-noir "0.9.9"]]

  :repl-options {:init-ns todo-list.core}
  :main todo-list.core

  :profiles {:dev
             {:main todo-list.core/-dev-main}})
