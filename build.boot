(def project 'big-solutions/boot-mvn)
(def version "0.1.1")

(set-env! :source-paths #{"src"}
          :dependencies   '[[org.clojure/clojure "1.7.0"]
                            [org.apache.maven/maven-embedder "3.1.1"]

                            [boot/core "2.6.0" :scope "test"]
                            [onetom/boot-lein-generate "0.1.3" :scope "test"]
                            [adzerk/bootlaces "0.1.13" :scope "test"]])

(task-options!
 pom {:project     project
      :version     version
      :description "Run Maven commands from Boot"
      :url         "https://github.com/big-solutions/boot-mvn"
      :scm         {:url "https://github.com/big-solutions/boot-mvn"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}}
 push {:ensure-branch  "master"
       :ensure-release true
       :ensure-clean   true
       :gpg-sign       false
       :repo-map       {:url "https://clojars.org/repo/"
                        :username (System/getProperty "CLOJARS_USER")
                        :password (System/getProperty "CLOJARS_PASS")}})

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

(deftask deploy
         "Deploys the project to clojars"
         []
         (comp (build) (push)))
