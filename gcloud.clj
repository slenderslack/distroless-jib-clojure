(ns gcloud
  (:require [clojure.java.shell :as sh]))

;; needs gcloud auth login to be complete
(defn authorizer [_]
  {:username "oauth2accesstoken" :password (:out (sh/sh "gcloud" "auth" "print-access-token"))})
