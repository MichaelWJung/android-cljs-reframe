(ns app.android
  (:require [cljs.reader]
            [reagent.ratom :as ra]
            [re-frame.core :as rf]))

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

(defn setup-android-interaction [init-fn]
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

  (.on js/LiquidCore
       "initialize"
       (fn [init]
         (let [todos (into (sorted-map)
                           (-> init
                               js->clj
                               cljs.reader/read-string))]
           (init-fn todos)))))

(defn todos->store
  [todos]
  (.emit js/LiquidCore "store" (clj->js {:value (str todos)})))

(defn send-waiting-for-db []
  (.emit js/LiquidCore "waiting-for-db"))

(defn send-ready []
  (.emit js/LiquidCore "ready"))
