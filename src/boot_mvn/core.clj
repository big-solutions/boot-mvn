(ns boot-mvn.core
  {:boot/export-tasks true}
  (:require [boot.core :as boot :refer [deftask *boot-version*]]
            [boot.pod :as pod]
            [clojure.java.io :as io]))

(defn maven-pod* [version]
  (pod/make-pod {:dependencies [['org.apache.maven/maven-embedder version :scope "test"]]}))

(def maven-pod
  (memoize maven-pod*))


(deftask mvn
  "Run Maven commands from Boot"
  [A args ARGS str "Maven commands and options"
   V version VERSION str "Maven version"
   F file FILE str "File name (default is pom.xml)"]

  (let [tmp (boot/tmp-dir!)
        tmp-path (.getAbsolutePath tmp)
        pom-name (or file "pom.xml")]
    (fn middleware [next-handler]
     (fn handler [fileset]
       (boot/empty-dir! tmp)
       (let [pom (->> fileset
                          boot/input-files
                          (boot/by-name [pom-name])
                          first
                          boot/tmp-file
                          slurp)
             pod (maven-pod (or version "3.1.1"))]

          (spit (io/file tmp "pom.xml") pom)

          (pod/with-eval-in pod
                            (.doMain (new org.apache.maven.cli.MavenCli)
                                     (.split ^String ~args "\\s+")
                                     ~tmp-path System/out System/err))
          (next-handler (-> fileset
                            (boot/add-resource tmp)
                            boot/commit!)))))))
