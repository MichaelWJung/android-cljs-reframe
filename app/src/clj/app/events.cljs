(ns app.events
  (:require
    [app.db :refer [default-db]]
    [re-frame.core :as rf]
    [cljs.spec.alpha :as s]))

(defn check-and-throw
  "Throws an exception if `db` doesn’t match the Spec `a-spec`."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

(def check-spec-interceptor (rf/after (partial check-and-throw :app.db/db)))

(def todo-interceptors [check-spec-interceptor
                        (rf/path :todos)])

(defn allocate-next-id
  "Returns the next todo id.
  Assumes todos are sorted.
  Returns one more than the current largest id."
  [todos]
  ((fnil inc 0) (last (keys todos))))

(rf/reg-event-db
  :initialize-db
  (fn [_ _]
    default-db))

(rf/reg-event-db
  :set-showing
  [check-spec-interceptor (rf/path :showing)]
  (fn [old-showing-value [_ new-showing-value]]
    (keyword new-showing-value)))

(rf/reg-event-db
  :add-todo
  todo-interceptors
  (fn [todos [_ text]]
    (let [id (allocate-next-id todos)]
      (assoc todos id {:id id :title text :done false}))))

(rf/reg-event-db
  :toggle-done
  todo-interceptors
  (fn [todos [_ id]]
    (update-in todos [id :done] not)))

(rf/reg-event-db
  :save
  todo-interceptors
  (fn [todos [_ id title]]
    (assoc-in todos [id :title] title)))

(rf/reg-event-db
  :delete-todo
  todo-interceptors
  (fn [todos [_ id]]
    (dissoc todos id)))

(rf/reg-event-db
  :clear-completed
  todo-interceptors
  (fn [todos _]
    (let [done-ids (->> (vals todos)
                        (filter :done)
                        (map :id))]
      (reduce dissoc todos done-ids))))

(rf/reg-event-db
  :complete-all-toggle
  todo-interceptors
  (fn [todos _]
    (let [new-done (not-every? :done (vals todos))]
      (reduce #(assoc-in %1 [%2 :done] new-done)
              todos
              (keys todos)))))
