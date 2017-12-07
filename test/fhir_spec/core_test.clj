(ns fhir-spec.core-test
  (:require [clojure.test :refer :all]
            [fhir-spec.transitions :as transitions]))

(def Coverage-3
  {:policyHolder {:reference "http://benefitsinc.com/FHIR/Organization/CBI35"}
   :beneficiary {:reference "Patient/4"}
   :relationship {:coding [{:code "self"}]}
   :grouping
   {:subGroup "123"
    :group "CBI35"
    :classDisplay "Silver: Family Plan spouse only"
    :subGroupDisplay "Trainee Part-time Benefits"
    :subPlanDisplay "Includes afterlife benefits"
    :class "SILVER"
    :groupDisplay "Corporate Baker's Inc. Local #35"
    :subClass "Tier2"
    :subPlan "P7"
    :plan "B37FC"
    :subClassDisplay "Low deductable, max $20 copay"
    :planDisplay "Full Coverage: Medical, Dental, Pharmacy, Vision, EHC"}
   :type
   {:coding
    [{:system "http://hl7.org/fhir/v3/ActCode"
      :code "EHCPOL"
      :display "extended healthcare"}]}
   :resourceType "Coverage"
   :subscriber {:reference "Patient/4"}
   :payor [{:reference "Organization/2"}]
   :status "active"
   :id "9876B1"
   :sequence "1"
   :identifier [{:system "http://benefitsinc.com/certificate", :value "12345"}]
   :period {:start "2011-05-23", :end "2012-05-23"}
   :dependent "1"
   :text
   {:div
    "sample text"
    :status "generated"}})

(def Coverage-2
  {:type
   {:system "http://hl7.org/fhir/v3/ActCode"
    :code "EHCPOL"
    :display "extended healthcare"}
   :resourceType "Coverage"
   :subscriber {:reference "Patient/4"}
   :id "9876B1"
   :sequence 1
   :identifier [{:system "http://benefitsinc.com/certificate", :value "12345"}]
   :issuer {:reference "Organization/2"}
   :period {:start "2011-05-23", :end "2012-05-23"}
   :dependent 1
   :subPlan "123"
   :plan "CBI35"
   :text
   {:div "sample text"
    :status "generated"}})

(def Coverage-3-for-2 ;; since no fields were renamed it works
  (select-keys Coverage-3 (keys Coverage-2))) ;; Kick out added fields

;; harcoded with versions: "1.0.2" "3.0.1"
(deftest transit-test
  (are [from-resource to-resource] (= (transitions/transit "1.0.2" "3.0.1" from-resource) to-resource)
     Coverage-2 Coverage-3-for-2
     (select-keys Coverage-2 [:subscriberId :resourceType]) (select-keys Coverage-3-for-2 [:subscriberId :resourceType]) ;; Broken test case, no such field in test data
     (select-keys Coverage-2 [:issuer :resourceType]) (select-keys Coverage-3-for-2 [:resourceType]) 
     ;; Add recursive type transformation check
     ))
