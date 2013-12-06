(ns web.views
  (:require [hiccup.core :refer (html)]
            [hiccup.form :as f]
            [web.generation :as gen]
            ))

(defn layout
  [title & content]
  (html
   [:head [:title title]]
   [:body content]))

(defn main-page
  []
  (layout "Home page"
          [:div [:h1 "Automata Generator"]]
          [:div "Welcome to the automata generator"]
          (f/form-to [:post "/"]
                     (f/label "states" "DFA state transitions")
                     (f/text-area {:rows 3} "states")
                     [:br]
                     (f/label "input" "Input")
                     (f/text-area "input")
                     (f/submit-button "Evaluate"))
          [:p (gen/test)]))

(defn results-page
  [states input]
  (layout "Home page"
          [:div [:h1 "Automata Generator"]]
          [:div "Welcome to the automata generator"]
          (f/form-to [:post "/"]
                     (f/label "states" "DFA state transitions")
                     (f/text-area {:rows 3} "states")
                     [:br]
                     (f/label "input" "Input")
                     (f/text-area "input")
                     (f/submit-button "Evaluate"))
          [:div (str (gen/evaluate-dfa states input))]
          [:div "works"]))
