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
                     (f/submit-button "Evaluate"))))

(defn get-evaluated-value
  [states input]
  (let [result (gen/evaluate-dfa states input)]
    (if (nil? result)
      [:div "ERROR MESSAGE"]
      [:div result])))

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
          (get-evaluated-value states input)
          [:div "works yay!"]))
