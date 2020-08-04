(ns tictactoe.macros)

;; Apparently macros must be written in .clj or .cljc files.

;; My first macro! Ain't it pretty.
(defmacro set-timeout [body time]
  `(js/setTimeout #(~@body) ~time))