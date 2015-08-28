# cljs-server

`cljs-server` is a code server to be used in conjunction with [chrome-clojurescript-repl](https://github.com/whamtet/chrome-clojurescript-repl).

## Usage

    lein new cljs-server my-project
    cd my-project
    lein cljsserve

this will start a server on localhost and compile your clojurescript code.  You can evaluate the code within [chrome-clojurescript-repl](https://github.com/whamtet/chrome-clojurescript-repl) by typing `(load "my-project.core")`.  This provides an easy development workflow for modifying other people's websites.

## Gotchas

Modern websites sometimes include a http header called Content Security Policy (CSP) that blocks cljs-server.  Check the Block CSP checkbox and refresh your page.

Enable the following flags on chrome [#allow-insecure-localhost](chrome://flags/#allow-insecure-localhost).


## License

Copyright © 2015 Matthew Molloy

Distributed under the Eclipse Public License, the same as Clojure.
