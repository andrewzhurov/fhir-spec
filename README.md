# fhir-spec

A cljc library designed to bootstrap your FHIR resource interactions.

Is NOT a mature project, but a demo "how it'd look like".

## Backstory

FHIR provided us with great deal of specs on how to work with their resources, we sure shall facilitate it.

Specs we have:
1) Resources:    https://github.com/fhir-js/fhir-schema
2) Interactions: https://www.hl7.org/fhir/http.html#mime-type
3) Transitions version->version for resources
   On example of Patient (json version provided too): https://www.hl7.org/fhir/patient-version-maps.html
  
Those specs covers FHIR part of an app, leave place for bare business logic.

### What does exactly it cover?
(1), describing formats, make possible:

1.1 client; UI generation (result is generic)

1.2 client; validation

1.3 server; participates in setting communication interface (what data it awaits?)

1.4 server; validation

(2), telling which operations are supported and how to perform

2.1 server; finalizes the info we need for server interface creation (how to knock?)

2.2 client; this also nails client-required information of server to it's url (we know the rest)

(3), describing resource transitions, tells us how to achieve cross-version interoperability

And we're getting all that out of the bare box!



### But, speaking less abstract, how it'll look in Clojure?
We'd need to make those specs Clojure-friendly.

Now, there are different ways to achieve, with different degree of goodness, as I see:
1) is described in three formats: verbal, xml, json (JSONSchema)
You know right - we'd like the latter! So we can:
- convert JSONSchema => clojure.spec 
  (sadly there is no such converter => write one)
- use JSONSchema directly for Swagger description,validation and client validation (github.com/niquola/json-schema.clj)
  (I had no success with those)
- use JSONSchema for client validation only, FHIRBase for server 

2) interactions described verbal and easy to implement by hand
3) description of resource transitions exist in json, we shall create mapping description=>actual-transformation-operation


Now, there are some perfect scenarious, but I wasn't lucky to have whole-time-of-the-universe to implement those.

I made such decisions:
1) HANDWRITTEN. Stick with JSONSchema => clojure.spec, though skip this "write a converter" part (for now or ever) and use mock, hand written, clojure.spec data, just as we'd get out of converter
2) compojure route generation
3) HANDWRITTEN. the same mock (though some handwritten transformations will still present, as the spec doesn't carry complete instructions)

### Now, how my choices would look in app:
1.2 client validation

-- pupeno/free-form - Reagent on (optionally) re-frame

   It lets you enhance your forms usual forms

   For it's validation to work it waits from you three things: values, errors, callback on form vals state change

   val - a resource

   err - (conform resource-spec val)

   on-state-change - whatever you like

   You shall mark which inputs correspond to which resource fields, what to do on error.

   Client validation is set!


1.3, 1.4  clojure.spec's are attached to endpoints

2.1 compojure routes generation for resources

2.2 (resource->url base-url resource) => url  (yet to add (simple))

3 available as Ring middleware


It leaves us with very universal FHIR interaction solution

But the efforts to achieve it are great


I've not implemented it from top to bottom, it'd go beyond borders of sanity, but used mock in many parts to taste how the result would feel.


## Things todo

to finish the mock-demo experience:
- attach clojure.spec only to appropriate routes
- provide resource->url func
- turn on generative testing
- make beautiful Markdown README, not this wreckage


## Usage

### Server

[fhir-spec "version"]

(require '[fhir-spec.compojure])

(fhir-spec.compojure/generate-resources-endpoints [{:version fhir-version
                                                    :type :Patient
                                                    :ends {:read   #(println "Req on Patient Read" %)
                                                           :create #(println "Req on Patient Create" %)
                                                           :patch  #(println "Req on Patient Patch" %)}}])



You've done good reading so far, you sure have some balls.

Any feedback is welcome - here, brownmoose3q@gmail.com or any way you know.
