(defproject clojure-reframe "0.1.0-SNAPSHOT"
  :source-paths ["src/clj"]
  :dependencies [[org.clojure/clojure       "1.10.1"]
                 [org.clojure/clojurescript "1.10.597"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [thheller/shadow-cljs      "2.8.83"]
                 [reagent                   "0.9.1"]
                 [re-frame                  "0.11.0-rc2"]]

  :plugins      [[lein-shadow          "0.1.7"]]

  :clean-targets ^{:protect false} [:target-path
                                    "shadow-cljs.edn"
                                    "package.json"
                                    "package-lock.json"
                                    "build-clj"]
  :shadow-cljs {:nrepl  {:port 8777}
                :builds {:app {:target     :browser
                               :optimizations :simple
                               ; :optimizations :advanced
                               :output-dir "build-clj/dev"
                               :compiler-options {:output-feature-set :es5}
                               ; :pretty-print true
                               :modules    {:app {:init-fn app.core/-main}}
                               }}}

  :aliases {"dev-auto" ["shadow" "watch" "app"]})
