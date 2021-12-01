(ns gcloud)

;; needs gcloud auth login to be complete
(defn authorizer [_]
  {:username "oauth2accesstoken" :password ""})
