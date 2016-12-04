(ns boot-mvn.core
  "Example tasks showing various approaches."
  {:boot/export-tasks true}
  (:require [boot.core :as boot :refer [deftask]])
  (:import (org.apache.maven.cli MavenCli)))

(deftask mvn
  "I'm a simple task with only setup."
  [A args ARGS str "the task argument"]
  (let [mvn (new MavenCli)
        params (.split ^String args "\\s+")]
    (println (vec params))
    (.doMain mvn params "." System/out System/err))
  identity)
