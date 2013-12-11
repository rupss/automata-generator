(ns web.views
  (:require [web.generation :as gen]
            [selmer.parser :refer :all]))

(def home-page "./src/web/views/home.html")
(def accept-page "./src/web/views/accept.html")
(def reject-page "./src/web/views/reject.html")
(def draw-script "./src/web/views/draw-js.html")
(def data "./src/web/views/data.js")

(def node-name-to-id (atom {}))

(def test-dfa {'S0 {:start true :result :accept :transitions {0 'S0 1 'S1}}
               'S1 {:result :reject :transitions {0 'S0 1 'S1}}})

(defn main-page
  []
  (render (slurp home-page) {:result nil :script nil}))

(defn make-node-line
  [node id]
  (swap! node-name-to-id assoc node id)
  (str "{id:" id ", label: '" node "'}"))

(defn construct-nodes
  [dfa]
  (let [nodes (keys dfa)
        ids (range (count nodes))
        node-lines (map make-node-line nodes ids)]
    (apply str (interpose "," node-lines))))

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
        edge-lines (map ( fn [[label target]] (make-edge-line source-state-name target label)) transitions)]
    (concat edge-list edge-lines)))

(defn get-edges
  [dfa]
  (let [edge-line-list (reduce add-edges [] dfa)]
    (apply str (interpose "," edge-line-list))))

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
   (nil? result) "ERROR"
   (= :accept result) (slurp accept-page)
   (= :reject result) (slurp reject-page)))

(defn results-page
  [states input]
  (let [[dfa result] (gen/evaluate-dfa states input)]
    (println "In results page")
    (println "dfa = " dfa)
    (println "result = " result)
    (render (slurp home-page) {:result (get-result-div (first result))
                               :script (construct-js dfa result)})))
