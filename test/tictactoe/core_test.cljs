(ns tictactoe.core-test
  (:require
   [cljs.test :refer-macros [deftest is]]
   [tictactoe.helper :refer [calc-index check-game]]))


(deftest calc-index-test
  (is (= 0 (calc-index 0 0)))
  (is (= 2 (calc-index 2 0)))
  (is (= 5 (calc-index 2 1))))

(deftest check-horizontal-test
  (let [game [:empty :empty :empty
              :circle :circle :circle
              :empty :empty :empty]]
    (is (= (check-game game) :circle))))

(deftest check-vertical-test
  (let [game [:cross :empty :empty
              :cross :empty :empty
              :cross :empty :empty]]
    (is (= (check-game game) :cross))))

(deftest check-diagonal-test
  (let [game [:cross :circle :empty
              :empty :cross :circle
              :circle :empty :cross]]
    (is (= (check-game game) :cross))))

(deftest check-draw-test
  (let [game [:cross :circle :cross
              :circle :cross :circle
              :circle :cross :circle]]
    (is (= (check-game game) :draw))))