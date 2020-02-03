(ns app.store)

(def store (atom nil))

(defprotocol Store
  (save [this todos]))

(defn todos->store
  [todos]
  (let [s @store]
    (if (nil? s)
      (throw (ex-info "Store uninitialized" {}))
      (save s todos))))
