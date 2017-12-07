(ns fhir-spec.transitions)

(declare transit-types)

(defmulti transit
  "Transit resource"
  (fn [from-version to-version resource] [from-version to-version (:resourceType resource)]))

(defmethod transit ["1.8.0" "3.0.1" "Patient"] [_ _ resource]
  (-> resource
      (clojure.set/rename-keys {:careProvider :generalPractitioner})))

(defmethod transit ["1.0.2" "3.0.1" "Coverage"] [from-version to-version resource]
  (cond-> resource
    :removed (dissoc :issuer :bin :group :plan :subPlan)
    (:type resource)  (update :type (fn [Coding] {:coding [Coding]}))
    ;; subscriptionId
    (:dependent resource) (update :dependent str)
    (:sequence resource)  (update :sequence  str)
    ;; network
    :update-complex-typse
    ((partial transit-types from-version to-version [{:key :identifier
                                                      :type-name "Identifier"}
                                                     {:key :period
                                                      :type-name "Period"}]))
    ))


(declare transit-type)
(defn transit-types ;; Probably we could instead put some metadata in types and reuse 'transit'
  [from-version to-version fields entity]
  (reduce (fn [acc {:keys [key type-name]}]
            (if (get acc key)
              (update acc key (partial transit-type from-version to-version type-name))
              acc))
          entity fields))

(defmulti transit-type
  "Transit type
   'Complex types' don't carry :resourceType field, so we need to supply it as well, in order to determine which type is it
   We will get info about which type it is from spec, simply looking at it (in case we're writing transitions by hand)
   or fetching it, in case of autogeneration process. Also we could use clojure.spec/conform to determine, though it'd consume lots"
  (fn [from-version to-version type-name type] [from-version to-version type-name]))

(defmethod transit-type ["1.0.2" "3.0.1" "Identifier"] [_ _ _ type] type) ;; No change
(defmethod transit-type ["1.0.2" "3.0.1" "Period"]     [_ _ _ type] type) ;; No change

