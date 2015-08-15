# cljs-server

ClojureScript is now self-bootstrapping.  That leaves open the problem of dependency management.  One solution is to run a normal Clojure Server that simply serves up the cljs source for compilation.  This project runs a server with a single endpoint `https://localhost:5000/?ns=my.ns` which returns the source code in my.ns.  You may write your own source code and put dependencies in project.clj.

## Usage

To start a local web server for development you can either eval the
commented out forms at the bottom of `web.clj` from your editor or
launch from the command line:

    $ lein run -m cljs-server.web


## License

Copyright © 2015 Matthew Molloy

Distributed under the Eclipse Public License, the same as Clojure.
