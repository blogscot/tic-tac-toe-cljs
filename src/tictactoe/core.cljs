(ns ^:figwheel-hooks tictactoe.core
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [tictactoe.utils :refer [calc-index check-game calc-computer-move]])
  (:require-macros
   [tictactoe.macros :refer [run-after]]))

(defonce app-state (atom {:text "Tic Tac Toe"
                          :player1-symbol :circle
                          :player2-symbol :cross
                          :next :player1
                          :opponent :human
                          :status nil
                          :game (vec (take 9 (repeat :empty)))}))

(defn- get-app-element []
  (gdom/getElement "app"))

(defn- get-modal-element []
  (gdom/getElementByClass "modal-container"))

(defn- get-current-player
  "Returns current player, either :circle or :cross"
  []
  (let [next-player (@app-state :next)]
    (if (= next-player :player1)
      (@app-state :player1-symbol)
      (@app-state :player2-symbol))))

(defn- computer-opponent? []
  (= :computer (@app-state :opponent)))

(defn- update-game-status []
  (let [status (check-game (@app-state :game))]
    (swap! app-state assoc :status status)))

(defn- get-game-status
  "Returns either nil (game in progress) or end condition:
   winning player or draw."
  []
  (@app-state :status))

(defn- update-cell [pos]
  (let [cell-empty? (= :empty (get-in @app-state [:game pos]))
        game-over (get-game-status)]
    (when (and cell-empty? (not game-over))
      (swap! app-state assoc-in [:game pos] (get-current-player)))))

(defn- computer-turn []
  (let [game-over (get-game-status)]
    (if-not game-over
      (run-after
       1000
       (update-cell (calc-computer-move @app-state))
       (update-game-status)
       (swap! app-state assoc :next :player1)))))

(defn- next-turn
  "Swaps players' turns. Plays computer opponent if configured."
  []
  (let [current-player (@app-state :next)
        next (if (= current-player :player1) :player2 :player1)]
    (swap! app-state assoc :next next)
    (when (and (= current-player :player1) (computer-opponent?))
      (computer-turn))))

(defn- close-modal []
  (set! (.-style (get-modal-element)) "display: none;"))

(defn- set-player-symbol [symbol]
  (if (= symbol :circle)
    (swap! app-state assoc :player1-symbol :circle :player2-symbol :cross)
    (swap! app-state assoc :player1-symbol :cross :player2-symbol :circle)))

(defn- set-opponent [opponent]
  (swap! app-state assoc :opponent opponent))

;; Components

(defn- rect [x y color]
  ^{:key (str x y)}
  [:rect {:width 0.9
          :height 0.9
          :fill color
          :x x
          :y y
          :on-click (fn []
                      (update-cell (calc-index x y))
                      (update-game-status)
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
                        (swap! app-state assoc
                               :status nil
                               :game (vec (take 9 (repeat :empty))))
                        (when (and (computer-opponent?) (= :player2 (@app-state :next)))
                          (computer-turn)))} "New Game"])

(defn- game-status []
  [:span#status (let [player1-next? (= :player1 (@app-state :next))]
                  (condp = (get-game-status)
                    nil (str "Current Player: " (if player1-next? "Player 1" "Player 2"))
                    :draw "Game is a bust!"
                    (str (if player1-next? "Player 2" "Player 1") " wins!")))])

(defn- modal []
  [:div.modal-container
   [:div.modal
    [:div.details
     [:h1 "Tic Tac Toe"]]
    [:div#game-choices
     [:div#symbol-choice
      [:label {:style {:margin-right 18}} "Start as"]
      [:span
       [:button.btn {:on-click #(set-player-symbol :circle)} "O"]
       [:button.btn {:on-click #(set-player-symbol :cross)} "X"]]]
     [:div#opponent-choice
      [:label "Player vs"]
      [:span:btn-row
       [:button.btn {:on-click #(set-opponent :human)} "Player"]
       [:button.btn {:on-click #(set-opponent :computer)} "Computer"]]]]
    [:div#commands
     [:button.btn.btn-start {:style {:margin "1rem 0"}
                             :on-click close-modal} "Start"]]]])

(defn- game []
  [:div
   (modal)
   [:h1 (:text @app-state)]
   (game-status)
   (when (get-game-status)
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
