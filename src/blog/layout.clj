(ns blog.layout
  (:use (compojure.html gen page-helpers form-helpers)
        (clojure.contrib pprint str-utils)
        (blog config db util)))

(defn preview-div []
  [:div
   [:h4 "Preview"]
   [:div#preview]])

(defmulti url type)

(defmethod url :tag [tag]
  (str "/tag/" (tag :id)))

(defmethod url :category [cat]
  (str "/category/" (cat :id)))

(defmethod url :post [post]
  (str "/post/" (post :id)))

(defmethod url :default [x]
  (die "Don't know how to make a url out of a " (type x)))

(defn type-link [type]
  (re-gsub #":" "" (str type)))

(defn link [x]
  (link-to {:class (str (type-link (type x)) "-link" (:class x))
	    :style (:style x)}
           (url x)
           (x :title)))

(defn post-comments-link [post]
  (when post
    (link-to (str "/post/" (post :id) "#comments")
             (cl-format nil "~a Comment~:*~[s~;~:;s~]"
                        (count (post :comments))))))

(defn- calc-cloud [i min max]
  (+ 7
  (if (and (> i min) (> max min))
    (/ (* 23 (- i min)) (- max min))
    1)))

(defn- tag-cloud [tags]
  (if ( not (empty? tags))
  (let [tags-f (frequencies tags)
	min (reduce min (map #(second %) tags-f))
	max (reduce max (map #(second %) tags-f))
	total (reduce + (map #(second %) tags-f))
	]
    (map #(assoc (first %) :style (str "font-size:" (calc-cloud (second %) min max)) "px") 
	 tags-f))))

(defn- nav [admin]
  [:div.navigation
   [:ul "Categories"
    (map #(vector :li (link %)) (all-categories))]
   [:ul "Tags" [:li
    (map #(vector :span (link %)) (tag-cloud (all-tags)))]]
   [:ul "Meta"
    (comment "TODO"
      [:li (link-to "/archives" "Archives")]
      [:li (link-to "/tag-cloud" "Tag Cloud")])
    [:li (link-to "/rss.xml" "RSS")]]
   (if admin
     [:div.admin
      [:ul "Hello, " admin
       [:li (link-to "/admin/add-post" "Add Post")]
       [:li (form-to [:post "/admin/logout"]
              (submit-button "Log out"))]]
      [:ul "DB Status: "
       [:li [:strong (if-let [errors (db-watcher-status)]
                       [:div [:span.error "ERROR!"]
                        [:div errors]]
                       [:span.message "OK"])]]]]
     [:ul "Log in?"
      [:li (link-to "/admin/login" "Log in")]])])

(defn messages [flash]
  (list
   (when-let [e (:error flash)]
     [:div.error e])
   (when-let [m (:message flash)]
     [:div.message m])))

(defn page [admin title flash session & body]
  (html
   [:html
    [:head
     [:title SITE-TITLE " - " title]
     (include-css "/css/style.css")
     (include-js "/js/combined.js")] ;; magic
    [:body
     [:div#page
      [:div#header [:h1 (link-to SITE-URL SITE-TITLE)]]
      [:div#nav (nav admin)]
      [:div#body.body
       (messages flash)
       body]
      [:div#footer
       "Powered by "
       (link-to "http://clojure.org" "Clojure") " and "
       (link-to "http://github.com/weavejester/compojure" "Compojure") " and "
       (link-to "http://github.com/briancarper/cow-blog" "Cows") " and "
       (link-to "http://github.com/smartrevolution/clj-gae-datastore" "Google App Engine Datastore") "."]]]]))
