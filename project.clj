(defproject cowclj "1.0.2"
  :description "A blog written in Clojure for Google App Engine using Compojure. "
  :dependencies [[org.clojure/clojure "1.2.0-master-20100518.110252-71"]
                 [org.clojure/clojure-contrib "1.2.0-SNAPSHOT"]
		 ;[leiningen/lein-swank "1.1.0"]
                 [com.google.appengine/appengine-api-1.0-sdk "1.3.2"]
                 [com.google.appengine/appengine-tools-sdk "1.3.0"]
                 [compojure-gae "0.3.4-SNAPSHOT"]
                 [org.clojars.choas/appengine "0.2-SNAPSHOT"]
		 [rhino/js "1.7R2"]
		 [org.clojars.choas/clj-gae-datastore "0.1"]
		 [joda-time "1.6"]
		 ]
  :dev-dependencies [[swank-clojure "1.1.0"]
		     [com.google.appengine/appengine-api-1.0-sdk "1.3.2"]
		     [com.google.appengine/appengine-api-labs "1.3.2"]
		     [com.google.appengine/appengine-api-stubs "1.3.2"]
		     [com.google.appengine/appengine-local-runtime "1.3.2"]
		     [com.google.appengine/appengine-testing "1.3.2"]]
  :namespaces [blog.admin
	       blog.config
	       blog.db
	       blog.layout
	       blog.markdown
	       blog.pages
	       blog.rss
	       blog.server
	       blog.test
	       blog.datastore
	       blog.util
	       blog.logger]
  :compile-path "war/WEB-INF/classes"
  :library-path "war/WEB-INF/lib"
  )


