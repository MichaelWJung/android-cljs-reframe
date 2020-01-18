(ns money.core
  (:require [reagent.core :as reagent]
            [reagent.ratom :as ra :refer [reaction]]
            [re-frame.core :as rf]
            [re-frame.db :as rd]))

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

(def atoms (atom {}))

(defn- save-atom [a]
  (let [i (count @atoms)]
    (swap! atoms #(conj % [i a]))))

(defn- -main [& args]
  (js/setInterval (fn [] nil) 1000)

  (rf/dispatch-sync [:initialize])

  (.on js/LiquidCore
       "dispatch"
       (fn []
         (rf/dispatch [:increase])
         ))

  (let [a (ra/run! @(rf/subscribe [:count]))
        emit-fn (fn [new-state]
                  (.emit js/LiquidCore "countupdate" #js {:count new-state}))
        watch-fn (fn [key atom old-state new-state]
                   (emit-fn new-state))]
    (save-atom a)
    (emit-fn @a)
    (add-watch a :watcher watch-fn)
    )

  (.emit js/LiquidCore "ready")
  )

(set! *main-cli-fn* -main)
