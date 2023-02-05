(ns tglsnr.async
  (:require [cljs.core.async :as a]))


(defn- pipe- [<in xfs]
  (let [<out (a/chan)]
    (a/pipeline 1 <out (apply comp xfs) <in)
    <out))

(defn drain [<in]
  (a/go-loop []
    (when (not (nil? (a/<! <in))) (recur))))

(defn bypass
  [f] (map #(do (f %) %)))

(defn <<! [<in & xfs]
  (-> <in
      (pipe- xfs)
      (drain)))
