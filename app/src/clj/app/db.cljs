(ns app.db
  (:require [cljs.spec.alpha :as s]))

(s/def ::id int?)
(s/def ::title string?)
(s/def ::done boolean?)
(s/def ::todo (s/keys :req-un [::id ::title ::done]))
(s/def ::todos (s/and
                 (s/map-of ::id ::todo)
                 #(instance? PersistentTreeMap %)
                 ))
(s/def ::showing
  #{:all
    :active
    :done
    })
(s/def ::db (s/keys :req-un [::todos ::showing]))

(def default-db
  {:todos   (sorted-map)
   :showing :all})
