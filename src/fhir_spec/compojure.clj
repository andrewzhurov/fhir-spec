(ns fhir-spec.compojure
  (:require [fhir-spec.utils :as utils]
            [fhir-spec.transitions :refer [transit]]
            [fhir-spec.info :as info]
            fhir-spec.common
            [compojure.api.sweet :refer :all]
            [clojure.spec.alpha :as s]
            fhir-spec.resources)) 

(s/def ::interaction #{:read :vread :update :patch :delete ;; Instance level Interactions
                       :create ;; Type level Interactions
                       })
(def transitable-interactions #{:update :patch :create})
(s/def ::handler any?) ;; ifn? fails in macro
(s/def ::route #(instance? compojure.api.routes.Route %))

(s/def ::ends (s/map-of ::interaction ::handler))
(s/def ::desired-version fhir-spec.info/supported-resource-versions)
(s/def ::resource (s/merge :fhir-spec.common/resource (s/keys :req-un [::ends
                                                                       ::desired-version])))
(s/def ::resources (s/coll-of (s/and ::resource
                                     :fhir-spec.info/supported-resource)))
(s/def ::route-opts (s/keys :req-un [::desired-version]))
;; Middlewares

(defn transit-versions-middleware [from-version to-version handler]
  (fn [request]
    (if-let [resource (:body-params request)]
      (do (println "Transiting FHIR resource; from version:" from-version "; to version:" to-version  "; resource:" resource)
          (handler (assoc request :body-params (transit from-version to-version resource))))
      (handler request))))

;; Routes

(s/fdef resource-endpoints
        :args (s/cat :resource ::resource)
        :ret ::route)

#_(defmacro resource-endpoints ;; Version through clojure.spec, data-oriented, fails on server start
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
    `(context ~(str "/" (name type)) [] 
      (compojure.api.sweet/resource
       ~(apply merge
               ;; FIX: It will not contain multiple same methods (e.g. two :get)
              (map (fn [[interaction handler]]
                     {(to-http-method interaction)
                      {:summary (format "'%s' interaction" (name interaction)) 
                       :parameters {:path-params (path interaction)
                                    ;:body-params (utils/resource-spec resource)
                                    }
                       ;:responses {200 {:schema map?}}
                       :handler handler}})
                   ends))))))

(defn interaction->route-params [interaction]
  {:method  (case interaction
              :read 'GET
              :vread 'GET
              :update 'PUT
              :patch 'PATCH
              :delete 'DELETE
              :create 'POST)
   :path  (case interaction 
            :vread "/:id/_history/:vid"
            :create "/"
            "/:id")
   :transitable? (contains? transitable-interactions interaction)})

(defmacro resource-endpoints ;; Version through Schema, not data-oriented way
  "Creates resource endpoints
   About interactions: www.hl7.org/fhir/http.html"
  [{:keys [desired-version version type ends] :as resource}]
  `(context ~(str "/" version"/" (name type)) [] 
     :tags ~[version]
     ~@(map (fn [[interaction handler]]
              (let [{:keys [method path transitable?]} (interaction->route-params interaction)]
                (list method path []
                      :middleware (when (and transitable? (not= version desired-version))
                                    [`(partial transit-versions-middleware ~version ~desired-version)])
                      :summary (str (clojure.string/upper-case (name interaction)) " interaction")
                      handler)))
            ends)))


(s/fdef fhir-resources-flat
   :args (s/cat :flat-description ::resources)
   :ret ::route) 

(defmacro fhir-resources-flat
  "Awaits fully self-contained resource descriptions
   Use this macro when you have them generated
   For hand-written go look at human-friendly 'fhir-resources' !"
  [flat-description]
  `(context "/fhir-resources" []
     ;:coercion :spec
     :tags ["fhir-resources"]
    ~@(map (fn [resource-description]
             `(resource-endpoints ~resource-description)) flat-description)))


(s/def ::resources-tree-description
  (s/map-of fhir-spec.info/supported-resource-versions (s/map-of :fhir-spec.info/resource-name (s/map-of ::interaction ::handler))))

(s/fdef fhir-resources
   :args (s/cat :opts ::route-opts
                :tree-description ::resources-tree-description)
   :ret ::route)

(defmacro fhir-resources
  "Lets you specify your resources in a human-friendly way
   If description isn't written by hand consider 'fhir-resources-flat'
  
   Why desired-version is a mandatory opt?
   Problem: we need two additional things, aside of resource itself,
   to know how(if) to transition it: it's version and desired version.
   First one we get from the context of endpoint
   Second we cannot know ahead of time, it is state that shall be specified in-place."
  [opts tree-description]
  `(context "/fhir-resources" []
     :tags ["fhir-resources"]
     ~@(for [[version resource] tree-description
             [type    ends]     resource]
         `(resource-endpoints ~{:desired-version (:desired-version opts) :version version :type type :ends ends}))))
