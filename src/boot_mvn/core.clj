(ns boot-mvn.core
  {:boot/export-tasks true}
  (:require [boot.core :as boot :refer [deftask *boot-version*]]
            [boot.pod :as pod]
            [boot.util :as util]
            [clojure.java.io :as io])
  (:import [java.io File]))

;; (set! *warn-on-reflection* true)

;; http://stackoverflow.com/questions/28385197/maven-embedder-compiler-dependency-could-not-be-resolved-no-connector-factori
(def ^:private wagon-version "2.10")
(def ^:private aether-conn-version "1.1.0")
(def ^:private slf4j-version "1.7.22")

(defn maven-pod [embedder-version]
  (future (pod/make-pod (-> (boot/get-env)
                            (assoc :dependencies [['org.apache.maven/maven-embedder embedder-version]
                                                  ['org.eclipse.aether/aether-connector-basic aether-conn-version]
                                                  ['org.eclipse.aether/aether-transport-wagon aether-conn-version]
                                                  ['org.apache.maven.wagon/wagon-http wagon-version]
                                                  ['org.apache.maven.wagon/wagon-provider-api wagon-version]
                                                  ['org.slf4j/slf4j-simple slf4j-version]])))))

(deftask mvn
  "Run Maven commands from Boot

  Given that you are consciously using a build tool inside a build tool, there
  are going to be some bumps.

  The main one is about the output folder: by default Maven outputs to
  working-dir/target. In the default case (--working-dir is missing) your
  output will be materialized on the fileset, so don't be surprised if you see
  target/target when using the target task.

  If you want to change the name of Maven's output, you can add a profile to
  you pom.xml with content:

    <profiles>
      <profile>
        <id>boot-clj</id>
        <build>
          <directory>mvn-target</directory>
        </build>
      </profile>
    </profiles>

  And call the task with (assuming pom.xml in .):

    boot -B --source-paths . mvn -A \"-Pboot-clj clean install\" target

  This will materialize target/mvn-target.

  If --W|--working-dir is specified, this dir will be used instead. This means
  that the output will NOT be materialized on the fileset and, if no profile
  customizes build.directory, the Maven output will be in target/.

  Except that if you are using also Boot's target task, your final target/ will
  end up being overwritten by Boot and you won't see Maven output at all.  This
  means that when using --working-dir, you always want to use the profile
  approach.

  By the default if the pom file is not found a warning is emitted but the next
  task in the pipeline is called."

  [A args ARGS str "Maven commands and options"
   V version VERSION str "Maven version"
   F file FILE str "Pom file path (default is pom.xml)."
   W working-dir PATH str "The working dir, in this case the output won't be on the fileset"]

  (let [tmp ^File (when-not working-dir (boot/tmp-dir!))
        pom-name (or file "pom.xml")
        pod-future (maven-pod (or version "3.3.9"))]
    (fn middleware [next-handler]
      (fn handler [fileset]
        (when-not working-dir (boot/empty-dir! tmp))

        (let [working-dir (or working-dir (.getCanonicalPath tmp))]
          (let [pom-path-file (if-not working-dir
                                (some->> fileset
                                         boot/input-files
                                         (boot/by-path [pom-name])
                                         first
                                         boot/tmp-file)
                                (io/file pom-name))]
            (if (and pom-path-file (.exists pom-path-file) (.isFile pom-path-file))
              (let [pom-content (slurp pom-path-file)]
                (when-not working-dir
                  (spit (io/file working-dir "pom.xml") pom-content))

                (pod/with-eval-in @pod-future
                  (require '[boot.util :as util])
                  (require '[boot.pod :as pod])
                  (import [org.apache.maven.cli MavenCli])
                  (import [java.lang System])
                  ;; Necessary or an exception will be thrown
                  (System/setProperty "maven.multiModuleProjectDirectory" ~working-dir)

                  (util/dbug* "PWD %s\n" (System/getenv "PWD"))
                  (util/dbug* "M2_HOME %s\n" (System/getenv "M2_HOME"))
                  (util/dbug* "maven.repo.local %s\n" (System/getProperty "maven.repo.local"))
                  (util/dbug* "user.home %s\n" (System/getProperty "user.home"))
                  (util/dbug* "maven.multiModuleProjectDirectory %s\n" (System/getProperty "maven.multiModuleProjectDirectory"))
                  (util/dbug* "working-dir %s\n" ~working-dir)

                  (let [arg-split (.split ^String ~args "\\s+")]
                    (util/dbug* "MavenCli args %s\n" (util/pp-str arg-split))
                    (.doMain (MavenCli.) arg-split ~working-dir System/out System/err))))
              (util/warn "The %s file cannot be found, be sure the file exists and it is on the classpath (double-check your set-env!). Ignoring the Maven build...\n" pom-name)))
          (next-handler (if-not working-dir
                          (-> fileset
                              (boot/add-resource tmp)
                              boot/commit!)
                          fileset)))))))
