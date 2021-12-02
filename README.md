## Build Clojure app into Container Image using jib

Build uber-jar and then package it into a container image based on `gcr.io/distroless/java`.  This will push to a gcr.io project so requires an authed gcloud login.

```
$ gcloud auth login
$ clj -T:build uber
$ clj -T:jib jib-build
```

Atomist will be scanning at this point.  Run the application.

```
docker run --rm gcr.io/personalsdm-216019/distroless-jib-clojure:latest
```

[gene-kim-gist]: https://gist.github.com/realgenekim/fdcad45286d065cc559cd75a8f946ad4#file-jib-build-clj-L45
