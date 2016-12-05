(ns boot-mvn.core
  {:boot/export-tasks true}
  (:require [boot.core :as boot :refer [deftask]])
  (:import (org.apache.maven.cli MavenCli)))

(deftask mvn
  "Run Maven commands from Boot"
  [A args ARGS str "Maven commands and options"]
  (let [mvn (new MavenCli)
        params (.split ^String args "\\s+")]
    (println (vec params))
    (.doMain mvn params "." System/out System/err))
  identity)
