(ns web.views
  (:require [web.generation :as gen]
            [selmer.parser :refer :all]
            [web.macros :as m]))

(def home-page "./src/web/views/home.html")
(def accept-page "./src/web/views/accept.html")
(def reject-page "./src/web/views/reject.html")
(def error-page "./src/web/views/error.html")
(def draw-script "./src/web/views/draw-js.html")
(def data "./src/web/views/data.js")

(def node-name-to-id (atom {}))

(def test-dfa {'S0 {:start true :result :accept :transitions {0 'S0 1 'S1}}
               'S1 {:result :reject :transitions {0 'S0 1 'S1}}})

(defn main-page
  []
  (render (slurp home-page) {:result nil :script nil}))

(defn add-node-color
  [base-node-line dfa node]
  (if (= :accept (:result (node dfa)))
    (str base-node-line ", color: 'green'}")
    (str base-node-line "}")))

(defn make-node-line
  [node id dfa]
  (swap! node-name-to-id assoc node id)
  (let [base-node-line (str "{id:" id ", label: '" node "'")]
    (add-node-color base-node-line dfa node)))

(defn construct-nodes
  [dfa]
  (let [nodes (keys dfa)
        ids (range (count nodes))
        node-lines (map (fn [node id] (make-node-line node id dfa)) nodes ids)
        start-node-line '("{id:-1, label: 'START', color: '66CCFF'}")]
    (apply str (interpose "," (concat start-node-line node-lines)))))

(defn construct-nodes-arr
  [dfa]
  (str "var nodes = [" (construct-nodes dfa) "];"))

(defn get-node-id
  [name]
  (@node-name-to-id name))

(defn make-edge-line
  [source target label]
  (str "{from: " (get-node-id source) ", to: " (get-node-id target) ", label: '" label "'}"))

(defn add-edges
  [edge-list [source-state-name state]]
  (let [transitions (:transitions state)
        edge-lines (map (fn [[label target]] (make-edge-line source-state-name target label)) transitions)]
    (concat edge-list edge-lines)))

(defn get-edges
  [dfa]
  (let [edge-line-list (reduce add-edges [] dfa)
        start-state-name (m/get-start-state dfa)
        start-state-id (get-node-id start-state-name)
        start-edge (str "{from: -1, to: " start-state-id "}")]
    (apply str (interpose "," (concat (list start-edge) edge-line-list)))))

(defn construct-edges-arr
  [dfa]
  (str "var edges = [" (get-edges dfa) "];"))

(defn construct-data
  [dfa]
  (str (construct-nodes-arr dfa) "\n" (construct-edges-arr dfa)))

(defn construct-js
  [dfa result]
  (if (nil? result)
    nil
    (render (slurp draw-script) {:data (construct-data dfa)})))

(defn get-result-div
  [result]
  (cond
   (= :accept result) (slurp accept-page)
   (= :reject result) (slurp reject-page)
   (nil? result) "ERROR"
   :else (render (slurp error-page) {:msg result})))

(defn results-page
  [states input]
  (let [[dfa result] (gen/evaluate-dfa states input)]
    (println "In results page")
    (println "dfa = " dfa)
    (println "result = " result)
    (println "final result = " (get-result-div result))
    (render (slurp home-page) {:result (get-result-div result)
                               :script (construct-js dfa
                          result)})))
