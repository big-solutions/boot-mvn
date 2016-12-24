(def project 'big-solutions/boot-mvn)
(def version "0.1.5")

(set-env! :source-paths #{"src"}
          :dependencies   '[[org.clojure/clojure "1.7.0"]

                            [boot/core "2.6.0" :scope "test"]])

(task-options!
 pom {:project     project
      :version     version
      :description "Run Maven commands from Boot"
      :url         "https://github.com/big-solutions/boot-mvn"
      :scm         {:url "https://github.com/big-solutions/boot-mvn"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask build
  "Build and install the project locally."
  []
  (comp (pom) (aot :all true) (jar) (install)))

(use 'boot-mvn.core)
