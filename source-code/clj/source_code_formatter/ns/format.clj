
(ns source-code-formatter.ns.format
    (:require [fruits.string.api                 :as string]
              [io.api                            :as io]
              [source-code-formatter.ns.assemble :as ns.assemble]
              [source-code-formatter.ns.indents  :as ns.indents]
              [source-code-formatter.ns.utils    :as ns.utils]
              [source-code-map.api               :as source-code-map]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn rebuild-ns-directive
  ; @ignore
  ;
  ; @param (string) file-content
  ; @param (map) ns-declaration-map
  ; @param (keyword) directive
  ;
  ; @return (string)
  [file-content ns-declaration-map directive]
  (if-let [_ (-> ns-declaration-map directive :bounds)]
          (-> file-content (string/cut-range   (-> ns-declaration-map directive :bounds first)
                                               (-> ns-declaration-map directive :bounds second))
                           (string/insert-part (-> file-content (ns.assemble/assemble-ns-directive ns-declaration-map directive))
                                               (-> ns-declaration-map directive :bounds first))
                           (string/insert-part (-> file-content (ns.assemble/assemble-ns-directive-comments ns-declaration-map directive))
                                               (-> ns-declaration-map directive :bounds first (+ 3 (-> directive name count)))))
          (-> file-content)))

(defn format-ns-deps!
  ; @description
  ; Formats the dependency libspecs within the namespace declaration in the source code file at the given filepath.
  ;
  ; @param (string) filepath
  ;
  ; @usage
  ; (format-ns-deps! "my-namespace.clj")
  [filepath]
  ; The 'ns-declaration-map' has to be regenerated after each directive rebuilding,
  ; because the positons of other directives might be changed.
  (if-let [file-content (io/read-file filepath {:warn? true})]
          (letfn [(f0 [file-content directive]
                      (let [ns-declaration-map (-> file-content source-code-map/ns-declaration-map ns.utils/prepare-ns-declaration-map)]
                           (rebuild-ns-directive file-content ns-declaration-map directive)))]
                 (io/write-file! filepath (-> file-content (f0 :import)
                                                           (f0 :require)
                                                           (f0 :use))))))
