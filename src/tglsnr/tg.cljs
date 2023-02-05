(ns tglsnr.tg
  (:require [telegram :refer [TelegramClient Api]]
            ["telegram/events" :refer [NewMessage]]
            ["telegram/sessions" :refer [StringSession]]
            [cljs.core.async :refer [go chan >! <! close! go-loop]]
            [cljs.core.async.interop :refer-macros [<p!]]
            [tglsnr.tg-storage :refer [tgs-get tgs-list tgs-set]]
            [tglsnr.async-iterator :refer [to-chan]]
            [tglsnr.buffer :refer [unlimited-buffer]]))

(def PeerUser (.-PeerUser Api))
(def PeerChat (.-PeerChat Api))
(def PeerChannel (.-PeerChannel Api))

(defn <tclient [api-id api-hash session rclient]
  (let [tc (TelegramClient. (StringSession. session) api-id api-hash #js {:connectionRetries 5})]
    (go (<p! (.start tc #js {:onError #(.error js/console %)}))
        {:tclient tc
         :rclient rclient
         :api-id api-id
         :api-hash api-hash
         :session session})))

(defn peer->native [id peer-type]
  (let [nid (js/BigInt id)]
    (cond
      (= peer-type "PeerUser") (PeerUser. #js {:userId nid})
      (= peer-type "PeerChat") (PeerChat. #js {:chatId nid})
      (= peer-type "PeerChannel") (PeerChannel. #js {:channelId nid}))))

(defn <msgs-after [{tclient :tclient} {peer :peer peer-type :peer-type last :msg-id}]
  (to-chan (.iterMessages tclient
                          (peer->native peer peer-type)
                          #js {:offsetId (js/parseInt last) :reverse true})))

(defn get-peer [msg]
  (let [peer (.-peerId msg)
        peer-id (if (= (.-className peer) "PeerUser")
                  (some-> peer (.-userId) (.toString))
                  (some-> peer (.-channelId) (.toString)))]
    peer-id))

(defn raw->msg [update]
  (let [msg (if (= (.-className update) "Message")
              update
              (.-message update))
        from (some-> msg (.-fromId) (.-userId) (.toString))
        peer (get-peer msg)
        peer-class (-> msg (.-peerId) (.-className))
        id (some-> msg (.-id) (.toString))
        text (-> msg (.-message))]
    {:peer peer
     :id id
     :from (or from peer)
     :msg text
     :peer-type peer-class
     :public (= peer-class "PeerChannel")}))

(defn- tg-pipe
  ([rc from to] (tg-pipe rc from to true))
  ([rc from to close?]
   (go-loop []
     (let [v (<! from)]
       (if (nil? v)
         (when close? (close! to))
         (let [msg (raw->msg v)
               msg-id (:id msg)
               msg-peer (:peer msg)
               msg-valid (not (nil? msg-id))
               last-id (when msg-valid (:msg-id (<! (tgs-get rc msg-peer))))]
           (when (and msg-valid (< (js/parseInt (or last-id "0")) (js/parseInt msg-id)))
             (>! to msg)
             (tgs-set rc msg))
           (recur)))))
   to))

(defn <msgs [{tc :tclient rc :rclient :as tg}]
  (let [<event-chan (chan (unlimited-buffer))
        <history (tgs-list rc)
        <out (chan)]
    (go (.addEventHandler tc #(go (>! <event-chan %)) (NewMessage. #js {}))
        (loop []
          (let [item (<! <history)]
            (when (-> item (nil?) (not))
              (prn "Getting history for the record: " item)
              (tg-pipe rc (<msgs-after tg item) <out false)
              (recur))))
        (tg-pipe rc <event-chan <out))
    <out))
