# boot-mvn

A Boot task to do many wonderful things.

## Usage

FIXME: explanation

Run the `boot-mvn-pre` task:

    $ boot boot-mvn-pre

To use this in your project, add `[boot-mvn "0.1.0-SNAPSHOT"]` to your `:dependencies`
and then require the task:

    (require '[boot-mvn.core :refer [boot-mvn-pre]])

Other tasks include: `boot-mvn-simple`, `boot-mvn-post`, `boot-mvn-pass-thru`.

## License

Copyright © 2016 Big Solutions

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
