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
  "Run Maven commands from Boot"
  [A args ARGS str "Maven commands and options"
   V version VERSION str "Maven version"
   F file FILE str "File name (default is pom.xml)"]

  (let [tmp ^File (boot/tmp-dir!)
        tmp-path (.getAbsolutePath tmp)
        pom-name (or file "pom.xml")
        pod-future (maven-pod (or version "3.3.9"))]
    (fn middleware [next-handler]
      (fn handler [fileset]
        (boot/empty-dir! tmp)

        ;; Necessary or an exception will be thrown
        (System/setProperty "maven.multiModuleProjectDirectory" tmp-path)

        (util/dbug* "M2_HOME %s\n" (System/getenv "M2_HOME"))
        (util/dbug* "maven.repo.local %s\n" (System/getProperty "maven.repo.local"))
        (util/dbug* "user.home %s\n" (System/getProperty "user.home"))
        (util/dbug* "maven.multiModuleProjectDirectory %s\n" (System/getProperty "maven.multiModuleProjectDirectory"))

        (if-let [pom (->> fileset
                          boot/input-files
                          (boot/by-path [pom-name])
                          first)]
          (let [pom-content (-> pom
                                boot/tmp-file
                                slurp)]
            (spit (io/file tmp "pom.xml") pom-content)

            (pod/with-eval-in @pod-future
              (require '[boot.util :as util])
              (require '[boot.pod :as pod])
              (import [org.apache.maven.cli MavenCli])

              (let [arg-split (into-array String (.split ^String ~args "\\s+"))]
                (util/dbug* "MavenCli args %s\n" (util/pp-str arg-split))
                (.doMain (MavenCli.) arg-split ~tmp-path System/out System/err)))

            (next-handler (-> fileset
                              (boot/add-resource tmp)
                              boot/commit!)))
          (throw (ex-info (format "The %s file cannot be found, be sure the file exists and it is on the classpath (double-check your set-env!)" pom-name)
                          {:pom-name pom-name})))))))
