(ns droidbox.core.actions
   (:require
     [droidbox.groups.droidbox :refer [droidbox]]
     [pallet.compute :refer [images instantiate-provider nodes]]
     [pallet.crate :refer [admin-user]]
     [pallet.compute.vmfest :refer [add-image]]
     [clojure.pprint :refer [pprint]]
     [pallet.core.user :refer [*admin-user*]]
     [pallet.api :refer [group-spec server-spec node-spec plan-fn converge lift]])
   (:use [pallet.repl]))

(defn vmfest-install-ubuntu [vmfest]
  (when-not (contains? (images vmfest) :ubuntu-14.04)
    (add-image (vmfest "https://s3.amazonaws.com/vmfest-images/ubuntu-14.04.vdi.gz"))))

(defn install-ubuntu []
  ; https://s3.amazonaws.com/vmfest-images
  (vmfest-install-ubuntu (instantiate-provider "vmfest")))

(defn print-images []
  (let [vmfest (instantiate-provider "vmfest")]
    (pprint (images vmfest))))

(defn run []
  (let [vmfest (instantiate-provider "vmfest")]
    (vmfest-install-ubuntu vmfest)
    (let [s (converge {droidbox 1} :compute vmfest :user *admin-user*)]
      (show-nodes vmfest)
      s)))

(defn clean []
  (let [vmfest (instantiate-provider "vmfest")]
    (converge {droidbox 0} :compute vmfest)))