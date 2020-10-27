(ns kepler16.deploy
  (:require [clj-http.client :as client]
            [jsonista.core :as json]
            [clojure.java.io :as io]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zip-xml]))


(def query "
query ($repo: String!, $org: String!, $version: String!) {
  repository(name: $repo, owner: $org) {
    packages(packageType: MAVEN, first: 1) {
      edges {
        node {
          version(version: $version) {
            id
          }
        }
      }
    }
  }
}")

(defn package-exists? [{:keys [repo org version token] :as package}]
  (let [req (client/post
             "https://api.github.com/graphql"
             {:headers {:Content-Type "application/json"
                        :Authorization (str "Bearer " token)}
              :body (json/write-value-as-string
                     {:query query
                      :variables package})})]
    (some-> req
            :body
            json/read-value
            (get-in ["data" "repository" "packages" "edges" 0 "node" "version"]))))

(defn deploy-file [{:keys [group-id artifact repo-id repository-url jar-file pom]
                    :or {pom "pom.xml"
                         repo-id "github"
                         repository-url (str "https://maven.pkg.github.com/" group-id "/" artifact)}}]
  (str
   "mvn deploy:deploy-file"
   " -DrepositoryId=" repo-id
   " -Durl=" repository-url
   " -Dfile=" jar-file
   " -DpomFile=" pom))

(defn deploy-github-maven-package [{:keys [group-id artifact repo-id repository-url jar-file pom token version] :as package}]
  (if (package-exists? {:repo artifact
                        :org group-id
                        :version version
                        :token token})
   "echo \"Package already exists.\""
   (deploy-file package)))

(def root
  (-> (io/file "pom.xml")
      xml/parse
      zip/xml-zip))

(defn pom->group-id [pom-zipper]
  (first
   (zip-xml/xml->
    pom-zipper
    :groupId
    zip-xml/text)))

(defn pom->artifact [pom-zipper]
  (first
   (zip-xml/xml->
    pom-zipper
    :name
    zip-xml/text)))

(defn pom->version [pom-zipper]
  (first
   (zip-xml/xml->
    pom-zipper
    :version
    zip-xml/text)))

(println
 (deploy-github-maven-package {:token (System/getenv "GITHUB_TOKEN")
                               :group-id (pom->group-id root)
                               :artifact (pom->artifact root)
                               :repo-id "github"
                               :jar-file "target/lib.jar"
                               :version (pom->version root)}))
