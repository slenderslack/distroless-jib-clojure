(ns jib
  (:import
   (com.google.cloud.tools.jib.api Jib
                                   DockerDaemonImage
                                   Containerizer
                                   TarImage
                                   RegistryImage
                                   ImageReference CredentialRetriever Credential)
   (com.google.cloud.tools.jib.api.buildplan AbsoluteUnixPath)
   (com.google.cloud.tools.jib.frontend
    CredentialRetrieverFactory)
   (java.nio.file Paths)
   (java.io File)
   (java.util List ArrayList Optional)))

(defn- get-path [filename]
  (Paths/get (.toURI (File. ^String filename))))

(defn- into-list
  [& args]
  (ArrayList. ^List args))

(defn- get-path [filename]
  (Paths/get (.toURI (File. ^String filename))))

(defn- to-imgref [image-config]
  (ImageReference/parse (:image-name image-config)))

(defn add-registry-credentials [rimg registry-config]
  (cond
    (:username registry-config)
    (do (println "Using username/password authentication, user:" (:username registry-config))
        (.addCredential rimg (:username registry-config) (:password registry-config)))

    (:authorizer registry-config)
    (let [auth (:authorizer registry-config)]
      (println "Using custom registry authentication:" (:authorizer registry-config))
      (.addCredentialRetriever rimg (reify CredentialRetriever
                                      (retrieve [_]
                                        (require [(symbol (namespace (:fn auth)))])
                                        (let [creds (eval `(~(:fn auth) ~(:args auth)))]
                                          (Optional/of (Credential/from (:username creds) (:password creds))))))))

    :else rimg))

(defmulti configure-image (fn [image-config] (:type image-config)))

(defmethod configure-image :tar [{:keys [image-name]}]
  (println "Tar image:" image-name)
  (.named (TarImage/at (-> (File. ^String image-name)
                           .toURI
                           Paths/get))
          ^String image-name))

(defmethod configure-image :registry [{:keys [image-name] :as image-config}]
  (println "Registry image:" image-name)
  (-> (RegistryImage/named ^ImageReference (to-imgref image-config))
      (add-registry-credentials image-config)))

(defmethod configure-image :docker [{:keys [image-name] :as image-config}]
  (println "Local docker:" image-name)
  (DockerDaemonImage/named ^ImageReference (to-imgref image-config)))

(defmethod configure-image :default [image-config]
  (throw (Exception. ^String (str "Unknown image type: " (:image-name image-config)))))

(def default-base-image {:type :registry
                         :image-name "gcr.io/distroless/java"})

(defn jib-build
  "It places the jar in the container (or else it gets the hose again)."
  [_]
  (let [standalone-jar "target/server-0.1.1-standalone.jar"
        base-image {:image-name "gcr.io/distroless/java" 
                    :type :registry}
        target-image {:image-name "gcr.io/personalsdm-216019/distroless-jib-clojure" 
                      :authorizer {:fn 'gcloud/authorizer}
                      :type :registry}
        entrypoint ["java" "-jar"]
        app-layer [(into-list (get-path standalone-jar))
                   (AbsoluteUnixPath/get "/")]]
    (println "Building container upon" (:image-name base-image) "with" standalone-jar)
    (-> (Jib/from (configure-image base-image))
        (.addLayer (first app-layer) (second app-layer))
        (.setEntrypoint (apply into-list entrypoint))
        (.setProgramArguments (into-list "server-0.1.1-standalone.jar"))
        (.containerize (Containerizer/to (configure-image target-image))))))

