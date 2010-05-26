(ns blog.datastore
  (:use (clojure.contrib def str-utils))

  (:require 
   [com.freiheit.gae.datastore.datastore-access-dsl :as datastore]
   [com.freiheit.gae.datastore.datastore-query-dsl :as query]
   [com.freiheit.gae.datastore.datastore-types :as types]
   [blog.logger :as log]))

(datastore/defentity blog-entity
  [:id]
  [:title]
  [:markdown :pre-save types/to-text :post-load types/from-text]
  [:html :pre-save types/to-text :post-load types/from-text]
  [:ip]
  [:date] ;; TODO :pre-save (now)
  [:category]
)

(datastore/defentity tag-entity
  [:id]
  [:title]
  [:blog-id]
)

(datastore/defentity comment-entity
  [:id]
  [:post-id]
  [:author]
  [:email]
  [:url]
  [:markdown :pre-save types/to-text :post-load types/from-text]
  [:html :pre-save types/to-text :post-load types/from-text]
  [:ip]
  [:date] ;; TODO :pre-save (now)
)

(defn load-all-tags
  "Loads all tags from the datastore."
  []
  (query/select (query/where tag-entity [])))

(defn load-all-posts []
  (query/select (query/where blog-entity [])))

(defn load-post [id]
  (query/select (query/where blog-entity ([= :id id]))))

(defn store-post 
  "Stores blog to datastore."
  [blog]
  (let [post blog
	tags (:tags post)]

    (datastore/store-entities! [(make-blog-entity
				 :id (:id post)
				 :title (:title post)
				 :markdown (:markdown post)
				 :html (:html post)
				 :ip (:ip post)
				 :date (:date post)
				 :category (:title (:category post))
				 )])
    (datastore/store-entities! 
     (map #(make-tag-entity 
	    :id (:id %) 
	    :title (:title %) 
	    :blog-id (:id blog)) tags))
    post))

(defn exists-post? [id]
  (not (empty? (query/select (query/where tag-entity ([= :id id]))))))

(defn remove-post [id]
  (let [keys-blog (query/select-only-keys (query/where blog-entity ([= :id id])))
	keys-tags (query/select-only-keys (query/where tag-entity ([= :blog-id id])))]
    (datastore/delete-all! keys-blog)
    (datastore/delete-all! keys-tags)))

(defn load-comments [post-id]
  (query/select (query/where comment-entity ([= :post-id post-id]))))

(defn store-comment
  "Stores comment to datastore."
  [comment]
  (datastore/store-entities! [(make-comment-entity
			       :id (:id comment)
			       :post-id (:post-id comment)
			       :author (:author comment)
			       :email (:email comment)
			       :url (:url comment)
			       :markdown (:markdown comment)
			       :html (:html comment)
			       :ip (:ip comment)
			       :date (:date comment)
			       )])
  comment)
