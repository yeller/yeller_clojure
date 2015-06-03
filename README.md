# yeller-clojure-client

A Clojure library for sending exceptions to yellerapp.com

## Installation

## Basic Usage

Make a client with `yeller.clojure.client/client`:

```clojure
(require '[yeller.clojure.client :as yeller])

(def client (yeller/client {:token "YOUR API TOKEN HERE"}))
```

Report an exception:

```clojure
(yeller/report client my-exception)
```

There are a bunch of configuration options for the client, and a method for
attaching additional context to the exception report. See the doc strings for
`client` and `report` for more on those.

## Ring Middleware

This client ships with a ring middleware for automatically catching exceptions.
If you're building a ring app, likely this is how your app should interface
with yeller:

```clojure
(yeller.clojure.ring/wrap-ring my-handler {:token "YOUR TOKEN HERE"
                                           :environment "production"})
```

## Uncaught Exception Handler

Yeller's `client` returns an object that satisfies
`java.lang.Thread.UnhandledExceptionHandler`. To set it as the default uncaught
thread exception handler:

```clojure
(Thread/setDefaultUncaughtExceptionHandler yeller-client)
```

Note that it does *not* do this by default - touching global mutable state
across the JVM in libraries is real bad.

## Robustness

This client builds on top of [the java client](https://github.com/tcrayford/yeller_java)

As such, it inherits that client's retry functionality - if there's an error
communicating with yeller's servers, the client will retry multiple times.

## License

Copyright © 2014 Tom Crayford

Distributed under the Eclipse Public License version 1.0
