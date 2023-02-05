(ns tglsnr.tg-storage
  (:require [clojure.edn :as edn]
            [cljs.core.async :refer [go chan <! pipe buffer]]
            [tglsnr.redis :refer [r-get r-set r-scan]]))

; Converts {:key "key" :value "{:msg-id \"msg-id\" :peer-type \"peer-type\"""}"}
; to {:peer "peer-id" :msg-id "msg-id" :peer-type "peer-type"}
(def entity->struct
  (comp (map #(update % :value edn/read-string))
        (map #(let [peer (:key %)
                    res (:value %)]
                (assoc res :peer peer)))))

(defn tgs-set [rc {msg-id :id peer :peer peer-type :peer-type}]
  (r-set rc peer (prn-str {:msg-id msg-id :peer-type peer-type})))

(defn tgs-get [rc peer]
  (go (let [raw (<! (r-get rc peer))]
        (edn/read-string raw))))

(defn tgs-list [rc]
  (let [<out (chan (buffer 1) entity->struct)]
    (pipe (r-scan rc) <out)))

