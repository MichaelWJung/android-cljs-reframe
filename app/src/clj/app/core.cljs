(ns app.core
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

(def reactions (atom {}))

(defn- store-reaction [id reaction]
  (swap! reactions #(conj % [id reaction])))

(defn- get-and-remove-reaction [id]
  (loop []
    (let [old @reactions
          new (if (contains? old id)
                (dissoc old id)
                old)]
      (if (compare-and-set! reactions old new)
        (if (not= old new)
          (id old)
          nil)
        (recur)))))

(defn- -main [& args]
  (js/setInterval (fn [] nil) 1000)

  (rf/dispatch-sync [:initialize])

  (.on js/LiquidCore
       "dispatch"
       (fn [map]
         (let [event (update-in (js->clj map) [0] keyword)]
           (rf/dispatch event))))

  (.on js/LiquidCore
       "register"
       (fn [map]
         (let [{:strs [id query]} (js->clj map)
               query (update-in query [0] keyword)
               a (ra/run! @(rf/subscribe query))
               emit-fn (fn [new-state]
                         (.emit js/LiquidCore id (clj->js {:value new-state})))
               watch-fn (fn [_ _ _ new-state]
                          (emit-fn new-state))]
           (store-reaction (keyword id) a)
           (emit-fn @a)
           (add-watch a (keyword id) watch-fn))))

  (.on js/LiquidCore
       "deregister"
       (fn [id-string]
         (let [id (keyword (js->clj id-string))]
           (if-let [reaction (get-and-remove-reaction id)]
             (remove-watch reaction id)))))

  (.emit js/LiquidCore "ready")
  )

(set! *main-cli-fn* -main)
