(ns droidbox.groups.droidbox
    "Node defintions for droidbox"
    (:require
     [pallet.api :refer [group-spec server-spec node-spec plan-fn converge lift]]
     [pallet.crate.automated-admin-user :refer [automated-admin-user]]
     [pallet.compute :refer [images instantiate-provider nodes]]
     [pallet.compute.vmfest :refer [add-image]]
     [clojure.pprint :refer [pprint]]
     [pallet.actions :refer [package remote-directory]]
     [pallet.script.lib :as lib]
     [pallet.stevedore :refer [checked-script]]
     [pallet.crate.java :as java]
     [pallet.crate.lein :as lein]
     [pallet.crate.environment :refer [system-environment]]))

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
  ^{:doc "Components missing for various crates"}
  missing-prerequisites  
  (server-spec
    :phases
      {:configure
          (plan-fn 
            (pallet.actions/exec-script
              (lib/update-package-list)
              ; provides add-apt-repository required to install java
              (lib/install-package "python-software-properties")
              (lib/install-package "software-properties-common")
              ; required for remote-file in leiningen crate
              (lib/install-package "wget")
              ; general use
              (lib/install-package "unzip")
              )) }))

(def java-server
  (java/server-spec
    {:vendor :oracle
    ; :components #{:jdk}
    :version "7"}))

(def lein-installation
  (lein/leiningen {}))

(def
  ^{:doc "Define a server spec for droidbox"}
  droidbox-server
  (server-spec
   :extends [lein-installation]
   :phases
   {:configure (plan-fn
                 (remote-directory "android-adt"
                              ;:unpack :tar
                              ;:tar-options "-xzfM" ;"-xzfM"
                              :unpack :unzip
                              :url "http://dl.google.com/android/adt/adt-bundle-linux-x86_64-20140702.zip")
                 ;(lib/mv "android-adt/adt/adt-bundle-linux-x86_64-20140702" "android")
                 (system-environment "android-tools" {"PATH" "~/android/sdk/tools/:~/android/sdk/platform-tools/:$PATH"})
                 ; likely redundant:
                 (lib/export "PATH" "~/android/sdk/tools/:~/android/sdk/platform-tools/:$PATH")
                 ; Make zipalign available for older build tools:
                 (lib/cp "~/android/sdk/build-tools/android-4.4W/zipalign" "~/android/sdk/tools/")
                  )}))

(def
  ^{:doc "Defines a group spec that can be passed to converge or lift."}
  droidbox
  (group-spec
   "droidbox"
   :extends [base-server missing-prerequisites java-server droidbox-server]
   :node-spec default-node-spec))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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


(defn done []
  (let [vmfest (instantiate-provider "vmfest")]
    (pallet.api/converge {droidbox 0} :compute vmfest)))

(def sp 
  (group-spec "sys2"
         :phases 
         {:configure
         (plan-fn
          (system-environment "android-tools3" ["PATH" "~/android/sdk/tools/:~/android/sdk/platform-tools/:$PATH"] :shared true))}))

(defmacro exc [nm v]
  `(let [vmfest# (instantiate-provider "vmfest")
         sp# (group-spec ~nm :phases {:configure (plan-fn ~v)})
         s# (lift [sp#] :compute vmfest#)]
      (show-nodes vmfest#)
      (explain-session s#)))

; (lift (group-spec "droidbox2" :extends [droidbox-server-2] :node-spec default-node-spec) :compute (instantiate-provider "vmfest"))

; (exc "a2" (system-environment "android-tools3" ["PATH" "~/android/sdk/tools/:~/android/sdk/platform-tools/:$PATH"] :shared true :literal true))


; (def s (spin))



