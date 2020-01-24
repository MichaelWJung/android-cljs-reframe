(ns app.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
  :showing
  (fn [db _]
    (:showing db)))

(rf/reg-sub
  :sorted-todos
  (fn [db _]
    (:todos db)))

(rf/reg-sub
  :todos
  (fn [query-v _]
    (rf/subscribe [:sorted-todos]))
  (fn [sorted-todos query-v _]
    (vals sorted-todos)))

(rf/reg-sub
  :visible-todos
  :<- [:todos]
  :<- [:showing]
  (fn [[todos showing] _]
    (let [filter-fn (case showing
                      :active (complement :done)
                      :done :done
                      :all identity)]
      (filter filter-fn todos))))

(rf/reg-sub
  :completed-count
  :<- [:todos]
  (fn [todos _]
    (count (filter :done todos))))

(rf/reg-sub
  :footer-counts
  :<- [:todos]
  :<- [:completed-count]
  (fn [[todos completed] _]
    [(- (count todos) completed) completed]))
