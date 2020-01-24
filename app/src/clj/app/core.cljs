(ns app.core
  (:require [app.android :as a]
            [app.db]
            [app.events]
            [app.subs]
            [re-frame.core :as rf]))


(defn- prevent-exit-with-callback []
  (js/setInterval (fn [] nil) 1000))

(defn -main [& _]
  (prevent-exit-with-callback)
  (rf/dispatch-sync [:initialize-db])
  (rf/dispatch-sync [:add-todo "Müll rausbringen"])
  (rf/dispatch-sync [:add-todo "Termin ausmachen"])
  (rf/dispatch-sync [:add-todo "Katze füttern"])
  (rf/dispatch-sync [:add-todo "Terrasse streichen"])
  (rf/dispatch-sync [:add-todo "Brief schreiben"])
  (rf/dispatch-sync [:add-todo "Fahrrad reparieren"])
  (rf/dispatch-sync [:add-todo "Auto waschen"])
  (rf/dispatch-sync [:add-todo "Oma anrufen"])
  (rf/dispatch-sync [:add-todo "Festplatte formatieren"])
  (rf/dispatch-sync [:toggle-done 2])
  (rf/dispatch-sync [:toggle-done 8])
  (rf/dispatch-sync [:set-showing :active])
  (a/setup-android-interaction)
  (a/send-ready-to-android))

(set! *main-cli-fn* -main)
