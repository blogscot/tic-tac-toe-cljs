(ns ^:figwheel-hooks tictactoe.core
  (:require
   [goog.dom :as gdom]
   [goog.dom.classlist :as classlist]
   [goog.style :as style]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [tictactoe.utils :refer [calc-index check-game calc-computer-move]])
  (:require-macros
   [tictactoe.macros :refer [run-after]]))

(defonce app-state (atom {:text "Tic Tac Toe"
                          :player1 {:symbol :circle :wins 0}
                          :player2 {:symbol :cross :wins 0}
                          :next :player1
                          :opponent :human
                          :status nil
                          :game (vec (take 9 (repeat :empty)))}))

(defn- get-symbol-btn [] (gdom/getElement "circle"))
(defn- get-symbol-btn2 [] (gdom/getElement "cross"))
(defn- get-opponent-btn [] (gdom/getElement "human"))
(defn- get-opponent-btn2 [] (gdom/getElement "computer"))
(defn- get-modal-element [] (gdom/getElement "modal-container"))

(defn- get-current-player
  "Returns current player, either :circle or :cross"
  []
  (let [next-player (@app-state :next)]
    (if (= next-player :player1)
      (get-in @app-state [:player1 :symbol])
      (get-in @app-state [:player2 :symbol]))))

(defn- computer-opponent? []
  (= :computer (@app-state :opponent)))

(defn- update-game-status []
  (let [status (check-game (@app-state :game))
        current-player (@app-state :next)]
    (when (or (= status :circle) (= status :cross))
      (swap! app-state update-in [current-player :wins] inc))
    (swap! app-state assoc :status status)))

(defn- get-game-status
  "Returns either nil (game in progress) or end condition:
   winning player or draw."
  []
  (@app-state :status))

(defn- game-over? []
  (get-game-status))

(defn- update-scoreboard []
  (let [p1 (gdom/getElement "player1")
        p2 (gdom/getElement "player2")]
    (if (= (@app-state :next) :player1)
      (do
        (classlist/add p1 "playing")
        (classlist/remove p2 "playing"))
      (do
        (classlist/add p2 "playing")
        (classlist/remove p1 "playing")))))

(defn- update-cell [pos]
  (let [cell-empty? (= :empty (get-in @app-state [:game pos]))]
    (when (and cell-empty? (not (game-over?)))
      (swap! app-state assoc-in [:game pos] (get-current-player)))))

(defn- computer-turn []
  (if-not (game-over?)
    (run-after
     1000
     (update-cell (calc-computer-move @app-state))
     (update-game-status)
     (swap! app-state assoc :next :player1)
     (update-scoreboard))))

(defn- next-turn
  "Swaps players' turns. Plays computer opponent if configured."
  []
  (let [current-player (@app-state :next)
        next (if (= current-player :player1) :player2 :player1)]
    (swap! app-state assoc :next next)
    (update-scoreboard)
    (when (and (= current-player :player1) (computer-opponent?))
      (computer-turn))))

(defn- open-modal []
  (style/setStyle (get-modal-element) "display" "flex"))

(defn- close-modal []
  (style/setStyle (get-modal-element) "display" "none"))

(defn- set-player-symbol [symbol]
  (classlist/toggle (get-symbol-btn) "btn-selected")
  (classlist/toggle (get-symbol-btn2) "btn-selected")
  (if (= symbol :circle)
    (do
      (swap! app-state assoc-in [:player1 :symbol] :circle)
      (swap! app-state assoc-in [:player2 :symbol] :cross))
    (do
      (swap! app-state assoc-in [:player1 :symbol] :cross)
      (swap! app-state assoc-in [:player2 :symbol] :circle))))

(defn- set-opponent [opponent]
  (swap! app-state assoc :opponent opponent)
  (classlist/toggle (get-opponent-btn) "btn-selected")
  (classlist/toggle (get-opponent-btn2) "btn-selected"))

(defn- get-labels []
  (if (= (@app-state :opponent) :human)
    ["Player 1" "Player 2"]
    ["Player" "Computer"]))

;; Components

(defn- scoreboard [wins1 wins2]
  (let [[player1 player2] (get-labels)]
    [:div#scoreboard
     [:div
      [:div#player1 player1]
      [:div.score-text (str wins1)]]
     [:div
      [:div#player2 player2]
      [:div.score-text (str wins2)]]]))

(defn- rect [x y color]
  ^{:key (str x y)}
  [:rect {:width 0.9
          :height 0.9
          :fill color
          :x x
          :y y
          :on-click (fn []
                      (update-cell (calc-index x y))
                      (when-not (game-over?)
                        (update-game-status)
                        (next-turn)))}])

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

(defn- new-game-button []
  [:button#btn-new-game
   {:on-click (fn []
                (swap! app-state assoc
                       :status nil
                       :game (vec (take 9 (repeat :empty))))
                (when (and (computer-opponent?) (= :player2 (@app-state :next)))
                  (computer-turn)))} "New Game"])

(defn- reset-game-button []
  [:button#btn-reset {:on-click (fn []
                                  (swap! app-state assoc-in [:player1 :wins] 0)
                                  (swap! app-state assoc-in [:player2 :wins] 0)
                                  (swap! app-state assoc
                                         :next :player1
                                         :status nil
                                         :game (vec (take 9 (repeat :empty))))
                                  (open-modal))} "Reset"])

(defn- game-status []
  [:span#status (let [player1-next? (= :player1 (@app-state :next))
                      [player1 player2] (get-labels)]
                  (condp = (get-game-status)
                    nil ""
                    :draw "Game is a bust!"
                    (str (if player1-next? player2 player1) " wins!")))])

(defn- modal []
  [:div#modal-container
   [:div.modal
    [:div.details
     [:h1 "Tic Tac Toe"]]
    [:div#game-choices
     [:div#symbol-choice
      [:label {:style {:margin-right 18}} "Start as"]
      [:span
       [:button#circle.btn {:on-click #(set-player-symbol :circle)} "O"]
       [:button#cross.btn {:on-click #(set-player-symbol :cross)} "X"]]]
     [:div#opponent-choice
      [:label "Player vs"]
      [:span
       [:button#human.btn {:on-click #(set-opponent :human)} "Player"]
       [:button#computer.btn {:on-click #(set-opponent :computer)} "Computer"]]]]
    [:div#commands
     [:button.btn.btn-start {:style {:margin "1rem 0"}
                             :on-click close-modal} "Start"]]]])

(defn- game []
  [:div
   (modal)
   [:h1 (:text @app-state)]
   (game-status)
   [:span {:style {:visibility (if (game-over?) "initial" "hidden")}}
    (new-game-button)
    (reset-game-button)]
   (scoreboard
    (get-in @app-state [:player1 :wins])
    (get-in @app-state [:player2 :wins]))
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
  (when-let [el (gdom/getElement "app")]
    (mount el)
    (classlist/add (get-symbol-btn) "btn-selected")
    (classlist/add (get-opponent-btn) "btn-selected")
    (update-scoreboard)))

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
