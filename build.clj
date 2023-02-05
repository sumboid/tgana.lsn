(require '[cljs.build.api :as b])

(b/build "src"
         {:optimizations :simple
          :source-map false
          :main 'tglsnr.core
          :npm-deps {:telegram "2.11.5"
                     :dotenv "16.0.2"}
          :output-to "out/main.js"
          :target :nodejs})
