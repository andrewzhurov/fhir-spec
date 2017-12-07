(ns fhir-spec.utils
  (:require [clojure.spec.alpha :as s]
            fhir-spec.common
            fhir-spec.info))

(defn nsify [version]
  (as-> version $
      (clojure.string/replace $ #"\." "-")
      (str "v" $)))
(defn- spec-ns [version]
  (str "fhir-spec." (nsify version) ".resources"))

(s/fdef resource-spec
  :args :fhir-spec.common/resource
  :ret string?)
(defn resource-spec [{:keys [version type]}]
  (keyword (spec-ns version) (name type)))

(s/fdef parse-version
   :args (s/cat :version :fhir-spec.info/version)
   :ret (s/+ int?))
(defn parse-version [version]
  (->> (clojure.string/split version #"\.")
       (map #?(:clj #(Integer. %)
               :cljs #(js/parseInt %)))))

(s/fdef version-comparator
   :args (s/cat :version-a :fhir-spec.info/version
                :version-b :fhir-spec.info/version)
   :ret int?)
(defn version-comparator [version-a version-b]
  (->> (map - 
            (parse-version version-a)
            (parse-version version-b))
       (filter (complement zero?))
       first))


(s/fdef transition-path
   :args (s/cat :from-version :fhir-spec.info/supported-resource-versions
                :to-version   :fhir-spec.info/supported-resource-versions)
   :ret (s/* :fhir-spec.info/supported-resource-versions))
(defn transition-path [from-version to-version]
  )
