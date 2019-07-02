(ns todo-list.validators.user-validator
  [:require [validateur.validation :refer :all]
   [noir.validation :as v]])

(def email-validator
  (validation-set
   (validate-with-predicate :email
                            #(v/is-email? (:email %))
                            :message-fn
                            (fn [validation-map]
                              (if (v/has-value? (:email validation-map))
                                "the emails format is incorrect"
                                "is a required field")))))

(def username-validator
  (validation-set
   (format-of :username
              :format #"^[a-zA-Z0-9_]*$"
              :message "Only letters, numbers and underscores"
              :blank-message "Is a required field")))

(def password-validator
  (validation-set
   (length-of :password
              :within (range 8 101)
              :message-fn
              (fn [type m attributes & args]
                (if (= type :blank)
                  "Is a required field"
                  "passwords must be between 8 and 100 characters")))))

(defn validate-signup [signup]
  ((compose-sets password-validator email-validator username-validator) signup))

(comment
  (validate-signup { :email "thedude@bides.net" :password "12345678"}))
