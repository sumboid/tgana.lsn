(ns tglsnr.init
  (:require "dotenv"
            [tglsnr.config :refer [init-config!]]))

(defn init! []
  (.config dotenv)
  (init-config!))

