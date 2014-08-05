(ns droidbox.groups.droidbox
    "Node defintions for droidbox"
    (:require
     [pallet.api :refer [group-spec server-spec node-spec plan-fn converge]]
     [pallet.crate.automated-admin-user :refer [automated-admin-user]]
     [pallet.compute :refer [images instantiate-provider nodes]]
     [pallet.compute.vmfest :refer [add-image]]
     [clojure.pprint :refer [pprint]]))

(def default-node-spec
  (node-spec
   :image {:image-id :ubuntu-14.04}
   :hardware {:min-cores 1 :min-ram 1024}))

(def
  ^{:doc "Defines the type of node droidbox will run on"}
  base-server
  (server-spec
   :phases
   {:bootstrap (plan-fn (automated-admin-user))}))

(def
  ^{:doc "Define a server spec for droidbox"}
  droidbox-server
  (server-spec
   :phases
   {:configure (plan-fn
                 ;; Add your crate class here
                 )}))

(def
  ^{:doc "Defines a group spec that can be passed to converge or lift."}
  droidbox
  (group-spec
   "droidbox"
   :extends [base-server droidbox-server]
   :node-spec default-node-spec))

(use 'pallet.repl)

(defn install-ubuntu []
  ; https://s3.amazonaws.com/vmfest-images
  (let [vmfest (instantiate-provider "vmfest")]
    (when-not (contains? (images vmfest) :ubuntu-14.04)
      (add-image (vmfest "https://s3.amazonaws.com/vmfest-images/ubuntu-14.04.vdi.gz")))))

(defn print-images []
  (let [vmfest (instantiate-provider "vmfest")]
    (pprint (images vmfest))))

(defn spin []
  (let [vmfest (instantiate-provider "vmfest")]
    (let [s (converge {droidbox 1} :compute vmfest)]
      (show-nodes vmfest)
      s)))

; (def s (spin))

