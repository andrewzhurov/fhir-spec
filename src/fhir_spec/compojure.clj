(ns fhir-spec.compojure
  (:require [fhir-spec.utils :as utils]
            [fhir-spec.transitions :refer [transit]]
            [fhir-spec.info :as info]
            fhir-spec.common
            [compojure.api.sweet :refer :all]
            [clojure.spec.alpha :as s])) 

(s/def ::interactions #{:read :vread :update :patch :delete ;; Instance level Interactions
                        :create ;; Type level Interactions
                        })
(s/def ::handler fn?)

;; Middlewares

(defn transit-versions-middleware [to-version handler]
  (fn [request]
    (let [resource         (:body-params request)
          from-version     (:fhir-version resource)]
      (println "Coercing FHIR resource; to version:" to-version "; from version:" from-version "; resource:" resource)
      (transit from-version to-version resource))))

(defn test-handler-fn [& all])

;; Routes

(s/def ::ends (s/map-of :interaction ::interaction :handler ::handler))
(s/def ::resource (s/merge :fhir-spec.common/resource (s/keys :req-un [::ends])))
(s/fdef resource-endpoints
        :args (s/cat :resource ::resource)
        :ret #(instance? compojure.api.routes.Route %))

(defn resource-endpoints
  "Creates resource endpoints
   About interactions: www.hl7.org/fhir/http.html"
  [{:keys [version type ends] :as resource}]
  (let [to-http-method (fn [interaction] (case interaction
                                           :read :get
                                           :vread :get
                                           :update :put
                                           :patch :patch
                                           :delete :delete
                                           :create :post))
        path (fn [interaction] (str "/"  (case interaction 
                                           :vread ":id/_history/:vid"
                                           ":id")))
        ]
    (context (str "/" (name type)) req
      (compojure.api.sweet/resource
       (apply merge
              {:coercion :spec} ;; FIX: It will not contain multiple same methods (e.g. two :get)
              (map (fn [[interaction handler]]
                     {(to-http-method interaction)
                      {:parameters {:path-params (path interaction)
                                    :body-params (utils/resource-spec resource)}
                       :handler handler}})
                   ends))))))


(s/fdef generate-resources-endpoints
   :args (s/cat :resources (s/* (s/and ::resource
                                       :fhir-spec.info/supported-resource)))
   :ret #(instance? compojure.api.routes.Route %)) 

(defn generate-resources-endpoints [resources]
  (context "/resources" req
    :summary "Generated resources interaction interface, by fhir-spec"
    (map resource-endpoints resources)))



(s/def ::name string?)
(s/def ::message string?)
