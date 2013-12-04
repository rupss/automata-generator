(ns web.generation
  (:require [hiccup.core :refer (html)]
            [hiccup.form :as f]
            [clojure.core.logic :as logic]
            [clojure.string :as str]))

(def test-input
  "s S0 START A
t 0 -> S1
t 1 -> S0
s S1
t 0 -> S4
 1 -> s5



")

(defn is-transition-line?
  [line]
  (let [[trans-tag char] (str/split line #"\s+")]
    (and (is-transition-tag? )))

(defn is-state-tag?
  [tag]
  (= (str/lower-case tag) "s"))

(def test-dfa {'S0 {:start true :result :accept :transitions {0 'S0 1 'S1}}
               'S1 {:result :reject :transitions {0 'S0 1 'S1}}})

(defn get-state-name
  "e.g s S1 or s S0 start A or s S1 A
  returns nil if the line is in the wrong format, the state name if not"
  [state-line]
  (let [[state-tag state-name & options] (str/split state-line #"\s+")]
    (if (is-state-tag? state-tag)
      state-name
      nil)))

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
  (let [[state-tag state-name & options] (str/split state-line #"\s+")
        result (merge (get-empty-state) (result-info options) (start-info options))]
    (println "UPDATED STATE = " result)
    result))

(defn valid-transition?
  [[trans-tag char arrow state :as trans]]
  (and (= (str/lower-case trans-tag) "t") (= "->" arrow)))

(defn add-transition
  "adds the transition encoded by trans-line into curr-state
   curr-state has format: {:result :accept/:reject :transitions {}}"
  [curr-state trans-line]
  (let [[trans-tag char arrow state :as trans] (str/split trans-line #"\s+")]
    (if (valid-transition? trans)
      (assoc curr-state :transitions (merge (curr-state :transitions) {char (symbol state)}))
      nil)))

(defn add-new-state
  [states curr-state-name curr-state]
  (merge states {(symbol curr-state-name) curr-state}))

(defn get-empty-state
  []
  {:result nil :transitions {}})

(defn is-end-line?
  [line]
  (= "end" (str/lower-case line)))

(defn update-args
  "returns updated [states curr-state-name curr-state]"
  [states curr-state-name curr-state line]
  (let [line (str/trim line)
        new-state-name (get-state-name line)
        post-trans-add-state (add-transition curr-state line)]
    (cond
     (or new-state-name (is-end-line? line))
     (let [updated-state (update-state line)]
       (if (or (not (nil? curr-state-name)) (is-end-line? line))
         [(add-new-state states curr-state-name curr-state)
          new-state-name
          updated-state]
         (do
           (println "\n...Update-args...\n")
           (println "updated state = " updated-state)
           (println "to return = " [states new-state-name updated-state])
           [states new-state-name updated-state])))
     post-trans-add-state
     [states curr-state-name post-trans-add-state]
     :else nil)))

(defn parse-dfa
  [state-transitions]
  (let [lines (conj (str/split state-transitions #"[\n]") "end")]
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
                [new-states new-curr-state-name new-curr-state :as result]
                (update-args states curr-state-name curr-state line)]
            (println "update args result = " result)
            (if (and (nil? new-states) (nil? new-curr-state-name) (nil? new-curr-state))
              nil
              (do
                (recur (inc i) new-states new-curr-state-name new-curr-state)))))))))

(defn test
  []
  (logic/run 10 [q] (logic/== q 2)))

(defn evaluate-dfa
  [states input]
  states)
