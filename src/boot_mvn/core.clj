(ns boot-mvn.core
  {:boot/export-tasks true}
  (:require [boot.core :as boot :refer [deftask *boot-version*]]
            [boot.pod :as pod]))

(defn maven-pod* [version]
  (pod/make-pod (update-in (boot/get-env)
                           [:dependencies]
                           conj ['org.apache.maven/maven-embedder version :scope "test"])))

(def maven-pod
  (memoize maven-pod*))


(deftask mvn
  "Run Maven commands from Boot"
  [A args ARGS str "Maven commands and options"
   V version VERSION str "Maven version"]
  (let [pod (maven-pod (or version "3.1.1"))]
    (pod/with-eval-in pod
                      (.doMain (new org.apache.maven.cli.MavenCli)
                               (.split ^String ~args "\\s+")"." System/out System/err)))
  identity)
