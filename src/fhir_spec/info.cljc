(ns fhir-spec.info
  (:require [clojure.spec.alpha :as s]))
               
(def supported-resource-versions #{"1.0.2" "1.8.0" "3.0.1"})
(def supported-resources {"3.0.1" #{:Patient :Coverage} ;; PHILOSOPHICAL Resource names to strings?
                          "1.8.0" #{:Patient}
                          "1.0.2" #{:Coverage}})

(defn resource-supported? [{:keys [version type]}]
  (contains? (get supported-resources version) type))

(def supported-transitions {:Patient {"1.8.0" ["3.0.1"]}  ;; PHILOSOPHICAL Resource names to strings?
                            :Coverage {"1.0.2" ["3.0.1"]}})

(s/def ::version #(re-matches #"(\d+\.?)*\d+" %))
(s/def ::supported-resource resource-supported?) ;; PHILOSOPHICAL Resource names to strings?
(s/def ::resource-name keyword?)
