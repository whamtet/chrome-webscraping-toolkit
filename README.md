# cljs-server

`cljs-server` is a code server to be used in conjunction with [chrome-clojurescript-repl](https://github.com/whamtet/chrome-clojurescript-repl).

## Usage

Create a new clojurescript project

```
lein new mies my-project
```

then add

```clojure
:plugins [[cljs-server "0.1.0-SNAPSHOT"]]
```
to project.clj.  Next run

```
lein cljsserver
```

this will start a server on localhost and compile your clojurescript code.  You can evaluate the code within [chrome-clojurescript-repl](https://github.com/whamtet/chrome-clojurescript-repl) by typing `(load "my-project.core")`.  This provides an easy development workflow for modifying other people's websies.


## License

Copyright © 2015 Matthew Molloy

Distributed under the Eclipse Public License, the same as Clojure.
