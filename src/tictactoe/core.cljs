(ns ^:figwheel-hooks tictactoe.core
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [tictactoe.utils :refer [calc-index check-game calc-computer-move]]
   [tictactoe.macros :refer [run-after]]))

(defonce app-state (atom {:text "Tic Tac Toe"
                          :next :circle
                          :opponent :player
                          :game (vec (take 9 (repeat :empty)))}))

(defn- get-app-element []
  (gdom/getElement "app"))

(defn- get-modal-element []
  (gdom/getElementByClass "modal-container"))

(defn- get-current-player []
  (get-in @app-state [:next]))

(defn- get-status
  "Returns either nil (game in progress) or end condition:
   winning player or draw."
  []
  (check-game (get-in @app-state [:game])))

(defn- update-cell [pos]
  (let [cell-empty? (= :empty (get-in @app-state [:game pos]))
        game-over (get-status)]
    (when (and cell-empty? (not game-over))
      (swap! app-state assoc-in [:game pos] (get-in @app-state [:next])))))

(defn- next-turn
  "Swaps players' turns."
  []
  (let [current-player (get-in @app-state [:next])
        opponent (get-in @app-state [:opponent])
        next (if (= current-player :circle) :cross :circle)
        game-over (get-status)]
    (swap! app-state assoc-in [:next] next)
    (when (and (= current-player :circle) (not game-over) (= opponent :computer))
      (run-after
       1000
       (update-cell (calc-computer-move @app-state))
       (swap! app-state assoc-in [:next] :circle)))))

(defn- close-modal []
  (set! (.-style (get-modal-element)) "display: none;"))

(defn- set-opponent [opponent]
  (swap! app-state assoc-in [:opponent] opponent)
  (close-modal))

;; Components

(defn- rect [x y color]
  ^{:key (str x y)}
  [:rect {:width 0.9
          :height 0.9
          :fill color
          :x x
          :y y
          :on-click (fn [] (update-cell (calc-index x y))
                      (next-turn))}])

(defn- circle [x y color]
  ^{:key (str x y)}
  [:circle {:cx (+ x 0.45)
            :cy (+ y 0.45)
            :r 0.38
            :fill "white"
            :stroke color
            :stroke-width 0.1}])

(defn- cross [x y color]
  ^{:key (str x y)}
  [:g {:stroke color
       :stroke-width 0.4
       :stroke-linecap "round"
       :transform
       (str "translate(" (+ 0.42 x) "," (+ 0.42 y) ") scale(0.3)")}
   [:line {:x1 -1 :y1 -1 :x2 1 :y2 1}]
   [:line {:x1 1 :y1 -1 :x2 -1 :y2 1}]])

(defn- cell [x y]
  (let [pos (calc-index x y)
        status (get-in @app-state [:game pos])]
    (condp = status
      :empty  (rect x y "green")
      :circle (circle x y "aqua")
      :cross  (cross x y "red"))))

(defn- reset-button []
  [:button {:on-click (fn []
                        (swap! app-state assoc-in [:game] (vec (take 9 (repeat :empty))))
                        (swap! app-state assoc-in [:next] :circle))} "New Game"])

(defn- game-status []
  [:span#status (condp = (get-status)
                  nil (str "Current Player: " (if (= :circle (get-current-player))
                                                "Circle"
                                                "Cross"))
                  :circle "Circle wins!"
                  :cross "Cross wins!"
                  :draw "Game is a bust!")])

(defn- modal []
  [:div.modal-container
   [:div.modal
    [:div.details
     [:h1 "Tic Tac Toe"]]
    [:div#opponent-choice "Player vs"]
    [:span:btn-row
     [:button.btn {:on-click #(set-opponent :player)} "Player"]
     [:button.btn {:on-click #(set-opponent :computer)} "Computer"]]]])

(defn- game []
  [:div
   (modal)
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

(defn- display-app-state []
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
