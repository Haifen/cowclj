(ns blog.db
  (:use (clojure.contrib def str-utils)
        (blog util markdown))
  (:require 
   [blog.datastore :as ds]
   [blog.logger :as log]))

(defonce posts (ref nil))

(defn- uuid []
  (str (java.util.UUID/randomUUID)))

(defn title-to-id [s]
  (when s
   (->> s
       (re-gsub #"\s" "-")
       (re-gsub #"[^-A-Za-z0-9_]" "")
       .toLowerCase)))

(defn- with-type [type x]
  (with-meta x {:type type}))

(defn- valid-id? [id]
  (re-matches #"^[-A-Za-z0-9_]+$" id))

(defn make-post [post]
  (if (and (valid-id? (post :id))
           (not (empty? (post :markdown))))
    (with-type :post
      (assoc post
        :date (or (post :date) (now))
        :html (markdown-to-html (post :markdown) false)))
    (die "Invalid post data.  You left something blank:  " post)))

(defn make-comment [c]
  (with-type :comment
    (assoc c
      :id (uuid)
;      :post-id (post :id)
      :date (or (c :date) (now))
      :html (markdown-to-html (:markdown c) true))))

(defn make-category [cat]
  (when cat
   (with-type :category
     (assoc cat
       :id (title-to-id (:title cat))))))

(defn make-tag [tag]
  (when tag
   (with-type :tag
     (assoc tag
       :id (title-to-id (:title tag))))))

(defn- make-post-with-tags [post]
  (let [tags (map #(make-tag %) (:tags post))]
    (assoc (make-post post) :tags tags)))

(defn- load-all-tags-from-datastore []
  (map #(with-type :tag %) (ds/load-all-tags)))

(defn- tags-with-blog-id [tags blog-id]
  (filter #(= (:blog-id %) blog-id) tags))

(defn- a-cat [cat]
  {:id cat})

(defn- reset-posts []
  (dosync (ref-set posts nil)))

(defn load-all-posts []
  "Loads all blogs from the datastore."
  []
  (let [tags (load-all-tags-from-datastore)]
    (reverse 
     (sort-by :date 
	      (map #(assoc % :category 
			   (make-category {
					   :title (:category %)}) 
			   :tags (tags-with-blog-id tags (:id %))) 
		   (ds/load-all-posts))))))

(defn all-posts []
  (if (not @posts)
    (do
      (dosync (ref-set posts 
		       (map #(with-type :post %) (load-all-posts))))
      (log/info "load all posts" (count @posts))))
  @posts)

(defn load-post [id]
  "Load all blogs from datastore."
  []
  (let [tags (load-all-tags-from-datastore)]
    (reverse 
     (sort-by :date 
	      (map #(assoc % 
		      :category (make-category {:title (:category %)}) 
		      :tags (tags-with-blog-id tags (:id %))) 
		   (ds/load-post id))))))

(defn get-post [id]
  (with-type :post (first (load-post id))))

(defn add-post [post]
  (when (ds/exists-post? (:id post))
    (die "A post with that ID already exists."))
  (ds/store-post (make-post-with-tags post))
  (reset-posts))


(defn remove-post
  "Cleans the datastore by selecting the keys of all blogs and then using these keys 
to delete the data."
  [id]
  (ds/remove-post id)
  (reset-posts))

(defn edit-post [old-id post]
  (dosync
   (when (not= old-id (:id post))
     (remove-post old-id))
   (remove-post (:id post)) ;; remove also "new" id
   (ds/store-post (make-post-with-tags post))
   (reset-posts)))

(defn all-categories []
  (sort-by :name
           (set (filter identity
                        (map :category (all-posts))))))

(defn get-category [cat]
  (first (filter #(= (:id %) cat)
                 (all-categories))))

(defn- tag-without-blog-id [tag]
  (with-type :tag
    (assoc {} :id (:id tag) :title (:title tag))))

(defn tags-without-blog-id [tags]
  (map #(tag-without-blog-id %) tags))

(defn all-tags []
  (tags-without-blog-id (mapcat :tags (all-posts))))

(defn get-tag [tag]
  (first (filter #(= (:id %) tag)
                 (all-tags))))

(defn all-posts-with-category [category]
  (filter #(= (:category %) category)
          (all-posts)))


(defn- tag-id [tags] (map #(:id %) tags))

(defn all-posts-with-tag [tag]
  (filter #(some #{(:id tag)} (tag-id (:tags %)))
          (all-posts)))

(defn get-comments [post]
  (do
    (log/info "get comments for post id " (post :id))
    (sort-by :date 
	     (map #(with-type :comment %)
		  (ds/load-comments (post :id))))))

(defn add-comment [comment]
  (do
    (log/info "add comment: " comment)
    (ds/store-comment (make-comment comment))))

(defn db-watcher-status 
  "Note: not used"
  [])
