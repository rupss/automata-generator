(ns web.generation
  (:require [hiccup.core :refer (html)]
            [hiccup.form :as f]
            [clojure.core.logic :as logic]
            [clojure.string :as str]))

(def test-input
  "S0 START A
0 ----> S1
1 ----> S0
S1
0 -> S4
1 -> s5
END


")

(defn is-state-line?
  [line]
  (if (re-find (re-matcher #"->" line))
    false
    true))

(def test-dfa {'S0 {:start true :result :accept :transitions {0 'S0 1 'S1}}
               'S1 {:result :reject :transitions {0 'S0 1 'S1}}})

(defn get-state-name
  "e.g S1 or S0 start A or S1 A"
  [state-line]
  (first (str/split state-line #"[\s]")))

(defn result-info
  [options]
  (if (some #(= "a" (str/lower-case %)) options)
    {:result :accept}
    {:result :reject}))

(defn start-info
  [options]
  (if (some #(= "start" (str/lower-case %)) options)
    {:start true}
    nil))

(defn update-state
  "adds info about whether the state is an accepting/rejecting state and start state"
  [state-line]
  (println "\n...Updating state....")
  (println "curr line = " state-line)
  (let [options (rest (str/split state-line #"[\s]"))
        result (merge (get-empty-state) (result-info options) (start-info options))]
    (println "UPDATED STATE = " result)
    result))

(defn add-transition
  "adds the transition encoded by trans-line into curr-state
   curr-state has format: {:result :accept/:reject :transitions {}}"
  [curr-state trans-line]
  (let [[char state] (map str/trim (str/split trans-line #"->"))]
    (assoc curr-state :transitions (merge (curr-state :transitions) {char (symbol state)}))))

(defn add-new-state
  [states curr-state-name curr-state]
  (merge states {(symbol curr-state-name) curr-state}))

(defn get-empty-state
  []
  {:result nil :transitions {}})

(defn is-end-line?
  [line]
  (= "end" (str/lower-case (str/trim line))))

(defn update-args
  "returns updated [states curr-state-name curr-state"
  [states curr-state-name curr-state line]
  (let [line (str/trim line)]
    (if (is-state-line? line)
      (do
        (let [new-state-name (get-state-name line)
              updated-state (update-state line)]
          (if (or (not (nil? curr-state-name)) (is-end-line? line))
            [(add-new-state states curr-state-name curr-state) new-state-name updated-state]
            (do
              (println "\n...Update-args...\n")
              (println "updated state = " updated-state)
              (println "to return = " [states new-state-name updated-state])
              [states new-state-name updated-state]))))
      [states curr-state-name (add-transition curr-state line)])))

(defn parse-dfa
  [state-transitions]
  (let [lines (str/split state-transitions #"[\n]")]
    (loop [i 0
           states {}
           curr-state-name nil
           curr-state (get-empty-state)]
      (println "-----")
      (println "I = " i)
      (println "states = " states)
      (println "curr state name = " curr-state-name)
      (println "CURR_STATE = " curr-state)
      (if (== i (count lines))
        states
        (do
          (println "LINE = " (nth lines i))
          (let [line (str/trim (nth lines i))
                [new-states new-curr-state-name new-curr-state]
                (update-args states curr-state-name curr-state line)]
            (println "*****")
            (println "new curr state = " new-curr-state)
           (recur (inc i) new-states new-curr-state-name new-curr-state)))))))

(defn test
  []
  (logic/run 10 [q] (logic/== q 2)))

(defn evaluate-dfa
  [states input]
  states)
