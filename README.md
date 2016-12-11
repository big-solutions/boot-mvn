# boot-mvn

A Boot task to run Maven commands.

## Usage

To use this in your project:

[![Clojars Project](https://img.shields.io/clojars/v/big-solutions/boot-mvn.svg)](https://clojars.org/big-solutions/boot-mvn)

and then require the task:

    (require '[boot-mvn.core :refer [boot-mvn]])

Run the `boot-mvn` task:

    Options:
      -h, --help             Print this help info.
      -A, --args ARGS        ARGS sets maven commands and options.
      -V, --version VERSION  VERSION sets maven version.
      -F, --file FILE        FILE sets file name (default is pom.xml).
    
e.g.

    $ boot mvn --args "clean compile install"
    $ boot mvn --args -V
    $ boot mvn --args jetty:run
    $ boot mvn --version 3.2.1 --args "compile install"
    
All examples assume that there is a pom.xml (or a differently named file as per `--file` parameter) available in the task fileset.

## License

Copyright © 2016 Big Solutions

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
