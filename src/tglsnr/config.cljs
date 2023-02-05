(ns tglsnr.config)

(def config (atom {}))

(defn- get-env [k] (-> js/process (.-env) (aget k)))
(defn init-config! []
  (reset! config {:telegram-app-id (int (get-env "TELEGRAM_APP_ID"))
                  :telegram-app-hash (get-env "TELEGRAM_APP_HASH")
                  :telegram-session (get-env "TELEGRAM_SESSION")
                  :redis-url (get-env "REDIS_URL")
                  :redis-db (get-env "REDIS_DB")}))
