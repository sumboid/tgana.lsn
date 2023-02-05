(ns tglsnr.redis
  (:require [redis :refer [createClient]]
            [cljs.core.async :as a :refer [go chan go-loop <! >! close!]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [tglsnr.async-iterator :refer [to-chan]]))

(defn <r-client [url db]
  (go
    (let [client (createClient #js {:url url :database db})]
      (.on client "error" js/console.error)
      (<p! (.connect client))
      client)))

(defn r-set [client key value]
  (go (<p! (.set client key value))))

(defn r-get [client key]
  (go (<p! (.get client key))))

(defn r-scan [client]
  (let [out (chan)
        <keys (to-chan (-> client (.scanIterator)))]
    (go-loop []
      (let [key (<! <keys)]
        (if (not (nil? key))
          (do (>! out {:key key :value (<! (r-get client key))})
              (recur))
          (close! out))))
    out))
