# boot-mvn

A Boot task to run Maven commands.

## Usage

To use this in your project:

[![Clojars Project](https://img.shields.io/clojars/v/big-solutions/boot-mvn.svg)](https://clojars.org/big-solutions/boot-mvn)

and then require the task:

    (require '[boot-mvn.core :refer [mvn]])

Run the `mvn` task:

    Options:
      -h, --help             Print this help info
      -A, --args ARGS        sets maven commands and options
      -V, --version VERSION  sets maven version
      -F, --file FILE        sets file name (default is pom.xml)
      -W, --working-dir PATH sets the working dir, in this case the output won't be on the fileset

e.g.

    $ boot mvn --args "clean compile install"
    $ boot mvn --args -V
    $ boot mvn --args jetty:run
    $ boot mvn --version 3.2.1 --args "compile install"

All examples assume that there is a pom.xml (or a differently named file as per `--file` parameter) available in the task fileset.

## Task description

> Run Maven commands from Boot
>
> Given that you are consciously using a build tool inside a build tool, there
> are going to be some bumps.
>
> The main one is about the output folder: by default Maven outputs to
> working-dir/target. In the default case (--working-dir is missing) your
> output will be materialized on the fileset, so don't be surprised if you see
> target/target when using the target task.
>
> If you want to change the name of Maven's output, you can add a profile to
> you pom.xml with content:
>
>     <profiles>
>       <profile>
>         <id>boot-clj</id>
>         <build>
>           <directory>mvn-target</directory>
>         </build>
>       </profile>
>     </profiles>
>
> And call the task with (assuming pom.xml in .):
>
>     boot -B --source-paths . mvn -A \"-Pboot-clj clean install\" target
>
> This will materialize both `target/` and `mvn-target/`.
>
> If --W|--working-dir is specified, this dir will be used instead. This means
> that the output will NOT be materialized on the fileset and, if no profile
> customizes build.directory, the Maven output will be in target/.

> Except that if you are using also Boot's target task, your final target/ will
> end up being overwritten by Boot and you won't see Maven output at all.  This
> means that when using --working-dir, you always want to use the profile
> approach.
>
> By the default if the pom file is not found a warning is emitted but the next
> task in the pipeline is called."

## License

Copyright © 2016 Big Solutions

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
