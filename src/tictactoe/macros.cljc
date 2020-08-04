(ns tictactoe.macros)

;; Apparently macros must be written in .clj or .cljc files.

;; My first macro! Ain't it pretty.
(defmacro run-after [time & body]
  `(js/setTimeout #(do ~@body) ~time))