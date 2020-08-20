(ns tictactoe.utils
  (:require [clojure.spec.alpha :as s]))

(s/def ::pos (s/and int? #(>= % 0) #(< % 3)))

(defn calc-index [x y]
  {:pre [(s/valid? ::pos x), (s/valid? ::pos y)]}
  (+ x (* 3 y)))

(defn calc-computer-move [app-state]
  (rand-nth (let [state (app-state :game)]
              (for [i (range (count state))
                    :when (= :empty (nth state i))]
                i))))

(defn- check-line [line game]
  (let [matches (map #(get game %) line)
        first-match (first matches)]
    (if (and (every? #(= (first matches) %) matches)
             (not= first-match :empty))
      first-match
      nil)))

(defn- moves-left? [game]
  (some #(= :empty %) game))

(def ^:private winning [[0 1 2] ; rows
                        [3 4 5]
                        [6 7 8]
                        [0 3 6] ; columns
                        [1 4 7]
                        [2 5 8]
                        [0 4 8] ; diagonals
                        [2 4 6]])

(defn check-game [game]
  (loop [index 0]
    (let [line (get winning index)
          result (check-line line game)]
      (if result
        result
        (if (< index (count winning))
          (recur (inc index))
          (if (moves-left? game) nil :draw))))))
