(ns ^:figwheel-hooks tictactoe.core
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [tictactoe.helper :refer [check-game]]))

(defn calc-index [x y]
  (+ x (* 3 y)))

(defonce app-state (atom {:text "Tic Tac Toe"
                          :next :circle
                          :game (vec (take 9 (repeat :empty)))}))

(defn get-app-element []
  (gdom/getElement "app"))

(defn current-player []
  (get-in @app-state [:next]))

(defn next-turn []
  (let [current (get-in @app-state [:next])
        next (if (= current :circle) :cross :circle)]
    (swap! app-state assoc-in [:next] next)))

(defn update-board [x y]
  (let [pos (calc-index x y)]
    (swap! app-state assoc-in [:game pos] (get-in @app-state [:next]))
    (next-turn)))

(defn cell [x y]
  (let [pos (calc-index x y)
        status (get-in @app-state [:game pos])
        color (condp = status
                :empty "green"
                :circle "aqua"
                :cross "red")]
    ^{:key (str x y)}
    [:rect {:width 0.9
            :height 0.9
            :fill color
            :x x
            :y y
            :on-click (fn [] (update-board x y))}]))

(defn reset-button []
  [:button {:on-click (fn []
                        (swap! app-state assoc-in [:game] (vec (take 9 (repeat :empty)))))} "Reset Game"])

(defn get-status []
  (check-game (get-in @app-state [:game])))

(defn game-status []
  [:span#status (condp = (get-status)
                  nil (str "Current Player: " (if (= :circle (current-player))
                                                "Circle"
                                                "Cross"))
                  :circle "Circle wins!"
                  :cross "Cross wins!"
                  :draw "Game is a bust!")])

(defn game []
  [:div
   [:h1 (:text @app-state)]
   (game-status)
   (when (get-status)
     (reset-button))
   [:center
    [:svg {:view-box "0 0 3 3"
           :width 500
           :height 500}
     (doall (for [x-cell (range 3)
                  y-cell (range 3)]
              (cell x-cell y-cell)))]]])

(defn mount [el]
  (rdom/render [game] el))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

(defn display-app-state []
  (println (str @app-state)))

(defn ^:before-load on-my-load []
  (display-app-state))

;; specify reload hook with ^;after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element)
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
