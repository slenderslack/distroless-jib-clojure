## Build Clojure app into Container Image using jib

Build uber-jar and then package it into a container image based on `gcr.io/distroless/java`.  This will push to a gcr.io project so requires an authed gcloud login.

```
$ clj -Ttools install io.github.atomisthq/jibbit '{:git/tag "v0.1.0"}' :as jib
$ gcloud auth login
$ clj -Tjib build :main atomist.server :repository gcr.io/personalsdm-216019/distroless-jib-clojure :target-authorizer jibbit.gcloud/authorizer
```

Atomist will be scanning at this point.  Run the application.

```
docker run --rm gcr.io/personalsdm-216019/distroless-jib-clojure:latest
```

