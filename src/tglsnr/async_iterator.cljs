(ns tglsnr.async-iterator
  (:require [cljs.core.async :as async :refer [go >! close!]]))

(defn- push-next
  [output value step]
  (go (when (>! output value)
        (step))))

(defn- take-value
  [element]
  (when (not (.-done element))
    (.-value element)))

(defn- onto-chan
  ([output iterator]
   (let [step #(onto-chan output iterator)]
     (.. iterator
         (next)
         (then
          (fn [element]
            (if-let [value (take-value element)]
              (push-next output value step)
              (close! output)))
          (fn [err]
            (push-next output err step)))))))

(defn to-chan
  [iterator]
  (let [iter-chan (async/chan)
        actual (if (not (nil? (.-next iterator)))
                 iterator
                 (js-invoke iterator js/Symbol.asyncIterator))]
    (onto-chan iter-chan actual)
    iter-chan))
