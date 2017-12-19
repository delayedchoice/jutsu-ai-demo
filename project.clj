(defproject jutsu-ai-demo "0.1.0-SNAPSHOT"
  :description "Demo"
  :global-vars {*warn-on-reflection* true}
  :jvm-opts ["-XX:MaxPermSize=256M" "-mx8000m" "-Dclojure.server.repl={:port 5555 :accept clojure.core.server/repl}"]
  :resource-paths ["resources"]
  :timeout 999999999
  :aot :all
  :lein-release {:deploy-via :clojars}
  :profiles {:dev {;:global-vars {*warn-on-reflection* true
                   ;              *unchecked-math* :warn-on-boxed}
                   :dependencies [[criterium "0.4.4"]]}}
  :dependencies [[org.clojure/clojure "1.8.0"]
;                 [byte-streams "0.2.3"]
                 [uncomplicate/neanderthal "0.17.0"]
                 [hswick/jutsu.ai "0.1.0-SNAPSHOT"]
                 [org.nd4j/nd4j-cuda-8.0-platform "0.8.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.1"]
                 [log4j/log4j "1.2.17" :exclusions [javax.mail/mail
                                                    javax.jms/jms
                                                    com.sun.jmdk/jmxtools
                                                    com.sun.jmx/jmxri]]
                 [hswick/jutsu.ai.ui "0.0.1"]
                 [org.clojure/data.csv "0.1.4"]
                ; [org.clojure/tools.cli "0.3.5"]
                 ])
