(def project 'big-solutions/boot-mvn)
(def version "0.1.0")

(set-env! :source-paths #{"src"}
          :dependencies   '[[org.clojure/clojure "1.7.0"]
                            [org.apache.maven/maven-embedder "3.1.1"]
                            [boot/core "2.6.0" :scope "test"]
                            [onetom/boot-lein-generate "0.1.3" :scope "test"]])

(task-options!
 pom {:project     project
      :version     version
      :description "Run Maven commands from Boot"
      :url         "https://github.com/yourname/boot-mvn"
      :scm         {:url "https://github.com/yourname/boot-mvn"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask build
  "Build and install the project locally."
  []
  (comp (pom) (aot :all true) (jar) (install)))

(use 'boot-mvn.core)

(deftask idea
         "Updates project.clj for Idea to pick up dependency changes."
         []
         (require 'boot.lein)
         (let [runner (resolve 'boot.lein/generate)]
           (runner)))
