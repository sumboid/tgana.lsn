(ns tglsnr.buffer
  (:require [cljs.core.async.impl.protocols :as impl]))

(deftype UnlimitedBuffer [arr]
  impl/Buffer
  (full? [_] false)
  (remove! [_] (.shift arr))
  (add!* [this x]
    (.push arr x)
    this)
  (close-buf! [_])
  cljs.core/ICounted
  (-count [_]
    (.-length arr)))

(defn unlimited-buffer []
  (UnlimitedBuffer. #js []))
