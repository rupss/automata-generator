(ns web.views
  (:require [hiccup.core :refer (html)]
            [hiccup.form :as f]
            [web.generation :as gen]))

(defn setup-navbar
  []
  [:nav.navbar.navbar-default
   {:role "navigation"}
   [:div.navbar-header
    [:button.navbar-toggle
     {:data-target "#bs-example-navbar-collapse-1",
      :data-toggle "collapse",
      :type "button"}
     [:span.sr-only "Toggle navigation"]
     [:span.icon-bar]
     [:span.icon-bar]
     [:span.icon-bar]]
    [:a.navbar-brand {:href "#"} "Automata Generator"]]
   [:div#bs-example-navbar-collapse-1.collapse.navbar-collapse
    [:ul.nav.navbar-nav.navbar-right
     [:li
      [:a
       {:href "http://www.rupsshankar.tumblr.com"}
       "By Rupa Shankar"]]
     [:li
      [:a
       {:href "http://www.github.com/rupss/"}
       [:img {:src "/images/GitHub-Mark-32px.png"}]]]
     [:li {:style "width:120px"}]]]])

(defn layout
  [title & content]
  (str
   "<!DOCTYPE html>"
   (html
    [:head
     [:title title]
     [:link {:href "css/bootstrap.min.css", :rel "stylesheet"}]
     [:link {:href "css/dfa.css", :rel "stylesheet"}]]
    [:body
     [:a {:href "http://www.github.com/rupss/automata-generator" :class "banner"} [:img {:alt "Fork me on GitHub"
                                               :src "/images/fork.png"
                                               :style
                                               "position: absolute; right: 0; border: 0; z-index:100;"}]]
     (setup-navbar)
     content
     [:script {:src "//ajax.googleapis.com/ajax/libs/jquery/1/jquery.min.js"}]
     [:script {:src "js/bootstrap.min.js"}]])))

(defn input-form
  [result]
  [:div.container
   [:div.col-xs-9
    [:div.row
     [:form
      {:method "post", :action "/"}
      [:div.container
       [:div.row
        [:div.col-xs-2
         [:label {:for "states"} "DFA state transitions"]]
        [:div.col-xs-4
         [:textarea#states {:rows "3", :name "states"}]]]]
      [:br]
      [:div.container
       [:div.row
        [:div.col-xs-2 [:label {:for "input"} "Input"]]
        [:div.col-xs-4 [:textarea#input {:name "input"}]]]]
      [:button.btn.btn-default {:type "submit"} "Submit"]]]]
   [:div#result.col-xs-3 result]])

(defn main-page
  []
  (layout "Home Page" (input-form nil)))

(defn get-accept
  []
  [:div.accept "ACCEPT"])

(defn get-reject
  []
  [:div.reject "REJECT"])

(defn get-evaluated-value
  [states input]
  (let [result (gen/evaluate-dfa states input)]
    (cond
     (nil? result) [:div "ERROR MESSAGE"]
     (= :accept (first result)) (get-accept)
     (= :reject (first result)) (get-reject)
     :else [:div "problem"])))

(defn results-page
  [states input]
  (layout "Result"
          [:script {:src "js/draw-dfa.js"}]
          (input-form (get-evaluated-value states input))
          [:div {:id "viz"}]))
