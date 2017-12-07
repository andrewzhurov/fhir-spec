(ns fhir-spec.common
  (:require fhir-spec.info
            [clojure.spec.alpha :as s]))

(s/def ::type keyword?)
(s/def ::version fhir-spec.info/supported-resource-versions)

(s/def ::resource (s/keys :req-un [::type ::version]))
