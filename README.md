# Tic Tac Toe Using CLJS

An implementation of Tic Tac Toe using ClojureScript and Reagent. This is a learn-by-doing project.

## Overview

The game shows the player two initial options 1) Play as either 'O' or 'X' and 2) Play against another player or against a computer. The computer will select any available empty square, so it's far from hard to beat. There is also a scoreboard to keep track of scores as you pummel your opponent.

Have fun.

The game is running via [github pages](https://blogscot.github.io/tic-tac-toe-cljs/).

## Development

Using VS Code and Calva the game is started by 'jacking in'. Alternatively, using `lein fig:build` will start up a local REPL and open the game in the default browser.

## Deployment

The game has been deployed via the `/docs` directory. To cross-compile the ClojureScript code into JavaScript use,

```
lein clean
lein fig:deploy
```

Currently, the html and css files need to be copied over by hand.

## License

Copyright © 2020 Blogscot
