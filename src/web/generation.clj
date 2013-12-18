(ns web.generation
  (:require [clojure.core.logic :as logic]
            [clojure.string :as str]
            [clojure.set :as set]
            [web.macros :as mac]
            [cheshire.core :refer :all]))

(def test-input
  "s S0 start A
t 0->S0
t 1->S1")

(def states-tracker (atom #{}))

;;;;;; State-transition parsing code

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

(defn get-empty-state
  []
  {:result nil :transitions {}})

(defn update-state
  "adds info about whether the state is an accepting/rejecting state and start state"
  [state-line]
  (let [[state-tag state-name & options] (str/split state-line #"\s+")
        result (merge (get-empty-state) (result-info options) (start-info options))]
    result))

(defn parse-transition
  [trans-line]
  (let [[unparsed state] (str/split (str/trim trans-line) #"\s*->\s*")
        [trans-tag char] (str/split unparsed #"\s+")]
    (if (= trans-tag "t")
      [char state]
      nil)))

(defn add-transition
  "adds the transition encoded by trans-line into curr-state
   curr-state has format: {:result :accept/:reject :transitions {}}"
  [curr-state trans-line]
  (let [[char state :as result] (parse-transition trans-line)]
    (if (nil? result)
      nil
      (do
        (swap! states-tracker conj (symbol state))
        (assoc curr-state :transitions (merge (curr-state :transitions) {char (symbol state)}))))))

(defn add-new-state
  [states curr-state-name curr-state]
  (merge states {(symbol curr-state-name) curr-state}))

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
      (if (== i (count lines))
        states
        (do
          (let [line (str/trim (nth lines i))
                [new-states new-curr-state-name new-curr-state :as result]
                (update-args states curr-state-name curr-state line)]
            (if (and (nil? new-states) (nil? new-curr-state-name) (nil? new-curr-state))
              nil
              (do
                (recur (inc i) new-states new-curr-state-name new-curr-state)))))))))

;;;;;;;;;;;;;;;;;;

(defn make-error-msg
  [missing-states]
  (let [missing-states-strs (apply str (interpose ", " (map str missing-states)))]
    (str "ERROR - undefined state&#47;s " missing-states-strs)))

(defn has-missing-states
  "Checks to see if there are any states referenced to by a transition that is not actually defined. "
  [dfa]
  (if (nil? dfa)
    nil
    (if (= @states-tracker (set (keys dfa)))
      nil
      (let [not-defined (set/difference @states-tracker (set (keys dfa)))]
        (make-error-msg not-defined)))))

(defn valid?
  [dfa]
  (let [missing-states (has-missing-states dfa)]
    (cond
     (nil? dfa) nil
     (not (nil? missing-states)) missing-states
     :else true)))

(defn separate-into-vec
  [input]
  (if (empty? input)
    []
    (into [] (map #(str %) (seq (str/split (str/trim input) #"\s+"))))))


(defn evaluate-dfa
  [states input]
    (let [dfa (parse-dfa states)
          correct-input (separate-into-vec input)
          validity (valid? dfa)]
      (if (true? validity)
        (let [result ((mac/build-automata-fn dfa) correct-input)]
          (vector dfa (first result))) 
        (vector nil validity))))
