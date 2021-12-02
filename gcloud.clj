(ns gcloud
  (:require [clojure.java.shell :as sh]
            [clojure.string :as s]))

;; needs gcloud auth login to be complete
(defn authorizer [_]
  {:username "oauth2accesstoken" :password (s/trim (:out (sh/sh "gcloud" "auth" "print-access-token")))})
