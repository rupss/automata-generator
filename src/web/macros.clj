(ns web.macros
  (:require [hiccup.core :refer (html)]
            [hiccup.form :as f]
            [clojure.core.logic :as logic]
            [clojure.string :as str]))

(def test-dfa {'S0 {:start true :result :accept :transitions {0 'S0 1 'S1}}
               'S1 {:result :reject :transitions {0 'S0 1 'S1}}})

(defn build-transition
  [[char state] fn-names]
  ;; (println "build-transition")
  ;; (println "char = " char)
  ;; (println "state = " state)
  `([[~char . ?rem#] ?out#] (~(fn-names state) ?rem# ?out#)))

(defn my-matche
  [state fn-names]
  ;(println "my-matche")
  (let [trans-vecs (seq (:transitions state))]
    (cons `([[] ~(:result state)])
          `(~@(map (fn [vec] (build-transition vec fn-names)) trans-vecs)))))

(defn write-state-function
  [[state-name state] fn-names]
  ;(println "write-state-fn")
  `(~(fn-names state-name) [input# out#]
                (logic/matche [input# out#]
                        ~@(my-matche state fn-names))))


(defn get-start-state
  [dfa]
  ;(println "get-start-state")
  (-> (filter (fn [[name state]] (-> state :start nil? not)) (seq dfa))
      first
      first))

(defn make-fn-name-map
  [dfa]
  ;(println "make-fn-name-map")
  (let [gensym-pairs (map #(hash-map % (gensym %)) (keys dfa))]
    (reduce conj {} gensym-pairs)))

(defn build-automata-fn
  [dfa]
  (let [fn-names (make-fn-name-map dfa)]
    (eval
     `(fn [input#]
        (logic/run 5 [q#]
                   (letfn [~@(map #(write-state-function % fn-names) (seq dfa))]
                     (~(fn-names (get-start-state dfa)) input# q#)))))))
