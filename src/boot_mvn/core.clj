(ns boot-mvn.core
  {:boot/export-tasks true}
  (:require [boot.core :as boot :refer [deftask]]
            [boot.pod :as pod]))

(deftask mvn
  "Run Maven commands from Boot"
  [A args ARGS str "Maven commands and options"
   V version VERSION str "Maven version"]
  (let [maven-version (or version "3.1.1")
        pod (pod/make-pod (update-in (boot/get-env)
                                     [:dependencies]
                                     conj `[org.apache.maven/maven-embedder ~maven-version]))]
    (pod/with-eval-in pod
                      (require '[boot.core :as boot])
                      (import org.apache.maven.cli.MavenCli)
                      (.doMain (new org.apache.maven.cli.MavenCli)
                               (.split ^String ~args "\\s+")"." System/out System/err)))
  identity)
