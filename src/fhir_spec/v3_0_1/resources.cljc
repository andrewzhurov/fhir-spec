(ns fhir-spec.v3-0-1.resources
  (:require #?(:clj  [clojure.spec.alpha :as s]
               :cljs [cljs.spec.alpha :as s])))

(def test-Patient {:resourceType "Patient",
                   :name [{:use "legal",
                           :text "Legal form of name",
                           :family "none",
                           :given ["Adam"],
                           :prefix ["Eje"],
                           :suffix ["je"],
                           :period {:start "2001-05-06", :end "2010-06-06"}}],
                   :gender "male",
                   :active true,
                   :generalPractitioner [{:reference "Organization/."}]})
;; Primitives
(s/def ::boolean boolean?)
(s/def ::code string? #_#(re-matches #"[^\\s]+ ([\\s]? [^\\s]+)*" %)) ;; Not correct regex
(s/def ::string string?)
(s/def ::dateTime #(re-matches #"-?[0-9]{4}(-(0[1-9]|1[0-2])(-(0[0-9]|[1-2][0-9]|3[0-1])(T([01][0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9](\.[0-9]+)?(Z|(\+|-)((0[0-9]|1[0-3]):[0-5][0-9]|14:00)))?)?)?" %))
(s/def ::reference #(re-matches #"((http|https)://([A-Za-z0-9\\\.\:\%\$]\/)*)?(Account|ActivityDefinition|AdverseEvent|AllergyIntolerance|Appointment|AppointmentResponse|AuditEvent|Basic|Binary|BodySite|Bundle|CapabilityStatement|CarePlan|CareTeam|ChargeItem|Claim|ClaimResponse|ClinicalImpression|CodeSystem|Communication|CommunicationRequest|CompartmentDefinition|Composition|ConceptMap|Condition|Consent|Contract|Coverage|DataElement|DetectedIssue|Device|DeviceComponent|DeviceMetric|DeviceRequest|DeviceUseStatement|DiagnosticReport|DocumentManifest|DocumentReference|EligibilityRequest|EligibilityResponse|Encounter|Endpoint|EnrollmentRequest|EnrollmentResponse|EpisodeOfCare|ExpansionProfile|ExplanationOfBenefit|FamilyMemberHistory|Flag|Goal|GraphDefinition|Group|GuidanceResponse|HealthcareService|ImagingManifest|ImagingStudy|Immunization|ImmunizationRecommendation|ImplementationGuide|Library|Linkage|List|Location|Measure|MeasureReport|Media|Medication|MedicationAdministration|MedicationDispense|MedicationRequest|MedicationStatement|MessageDefinition|MessageHeader|NamingSystem|NutritionOrder|Observation|OperationDefinition|OperationOutcome|Organization|Patient|PaymentNotice|PaymentReconciliation|Person|PlanDefinition|Practitioner|PractitionerRole|Procedure|ProcedureRequest|ProcessRequest|ProcessResponse|Provenance|Questionnaire|QuestionnaireResponse|ReferralRequest|RelatedPerson|RequestGroup|ResearchStudy|ResearchSubject|RiskAssessment|Schedule|SearchParameter|Sequence|ServiceDefinition|Slot|Specimen|StructureDefinition|StructureMap|Subscription|Substance|SupplyDelivery|SupplyRequest|Task|TestReport|TestScript|ValueSet|VisionPrescription)\/[A-Za-z0-9\-\.]{1,64}(\/_history\/[A-Za-z0-9\-\.]{1,64})?"))
(defn resource-reference [resources] ;; Weaker
  (let [re-gex (java.util.regex.Pattern/compile (str "(" (clojure.string/join "|" resources ) ").+"))]
    (println re-gex)
    (comp boolean (partial re-matches re-gex))))

;; Complex

(s/def ::Person (s/keys :req-un [::name
                                 ::gender]
                        :opt-un [::active
                                 ::generalPractitioner]))
(s/def :fhir-spec.3.0.1.resources.Person.generalPractitioner/reference (resource-reference ["Organization" "Practitioner"]))
(s/def ::generalPractitioner (s/* (s/keys :req-un [:fhir-spec.3.0.1.resources.Person.generalPractitioner/reference])))
(s/def ::active ::boolean)
(s/def ::gender (s/and ::code #{"male" "female" "other" "unknown"}))
(s/def ::name   (s/* ::HumanName))

(s/def ::HumanName (s/keys :req-un [::use
                                    ::text
                                    ::family
                                    ::given
                                    ::prefix
                                    ::suffix
                                    ::period]))
(s/def ::use ::code)
(s/def ::text ::string)
(s/def ::family ::string)
(s/def ::given (s/* ::string))
(s/def ::prefix (s/* ::string))
(s/def ::suffix (s/* ::string))
(s/def ::Period (s/keys :opt-un [::start
                                 ::end]))
(s/def ::period ::Period)


(s/def ::start ::dateTime)
(s/def ::end ::dateTime)
