(ns app.core
  (:require [app.android :as a]
            [re-frame.core :as rf]))

(rf/reg-event-db
  :initialize
  (fn [_ _]
    {:count 0}))

(rf/reg-event-db
  :increase
  (fn [db _]
    (update db :count inc)))

(rf/reg-sub
  :count
  (fn [db _]
    (:count db)))

(defn- prevent-exit-with-callback []
  (js/setInterval (fn [] nil) 1000))

(defn -main [& _]
  (prevent-exit-with-callback)
  (rf/dispatch-sync [:initialize])
  (a/setup-android-interaction)
  (a/send-ready-to-android))

(set! *main-cli-fn* -main)
