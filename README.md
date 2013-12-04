# automata-generator

A web app using Clojure and the Compojure framework that allows users to
define their own DFAs or NFAs, test them out on various inputs, and
visualize the automata. Uses core.logic (Clojure's logic programming
libraries) to implement the automata. 

The automata function generating code will be taken from here: 
https://github.com/rupss/automata/blob/master/src/automata/macros.clj

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

## License

Copyright © 2013 Rupa Shankar
