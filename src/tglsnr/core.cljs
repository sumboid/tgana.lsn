(ns tglsnr.core
  (:require [cljs.core.async :as a :refer [go <!]]
            telegram
            [tglsnr.init :refer [init!]]
            [tglsnr.config :refer [config]]
            [tglsnr.tg :refer [<tclient <msgs]]
            [tglsnr.redis :refer [<r-client]]
            [tglsnr.async :refer [bypass <<!]]))

(defn- start! []
  (go
    (let [app-id (:telegram-app-id @config)
          app-hash (:telegram-app-hash @config)
          session (:telegram-session @config)
          rc (<! (<r-client (:redis-url @config) (:redis-db @config)))
          tc (<! (<tclient app-id app-hash session rc))]
      (<<! (<msgs tc)
           (bypass #(prn "orig:" %))
           (filter :public)
           (bypass println)))))

(init!)

(println @config)
(start!)
