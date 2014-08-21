(ns droidbox.groups.droidbox
    "Node defintions for droidbox"
    (:require
     [pallet.api :refer [group-spec server-spec node-spec plan-fn converge lift]]
     [pallet.crate.automated-admin-user :refer [automated-admin-user]]
     [pallet.actions :refer [package package-manager remote-directory remote-file directory exec-script exec-checked-script setup-node]]
     [pallet.action :refer [with-action-options]]
     [pallet.script.lib :as lib :refer [config-root file user-home]]
     [pallet.stevedore :refer [script checked-script fragment]]
     [pallet.core.user :refer [*admin-user*]]
     [pallet.crate :refer [defplan admin-user]]
     [pallet.crate.java :as java]
     [pallet.crate.lein :refer [lein leiningen]]
     [pallet.crate.environment :refer [system-environment]]))

(def default-node-spec
  (node-spec
   :image {:image-id :ubuntu-14.04}
   :hardware {:min-cores 1 :min-ram 1024}))

(defplan extend-path
  [label subpath ]
  ; note: system-environment by default appends to /etc/environment which doesn't expand vars nor is updated on the automatic relogin after the command
  (system-environment label {"PATH" (str subpath ":$PATH")} :shared true :literal true :path "/etc/profile")) 

(def
  ^{:doc "Defines the type of node droidbox will run on"}
  base-server
  (server-spec
   :phases
   {:bootstrap (plan-fn 
                (automated-admin-user))}))

(def 
  ^{:doc "Components missing for various crates"}
  missing-prerequisites  
  (server-spec
    :phases
      {:configure
          (plan-fn 
           (extend-path "lein-env" "/usr/local/bin/")
            (package-manager :update)
            (package "wget") ; required for remote-file in leiningen crate
            (package "unzip") ; general use
            (pallet.actions/exec-script
              (lib/install-package "python-software-properties")
              (lib/install-package "software-properties-common"))) }))

(def java-server
  (java/server-spec
    {:vendor :oracle
    ; :components #{:jdk}
    :version "7"}))

(defplan lein-droid-deploy
  [sdk-path]
  (setup-node)
  (with-action-options {:script-prefix :no-sudo}
    (lein "self-install")
    (remote-file (fragment (file ".lein" "profiles.clj")) 
                 :content (pr-str {:user {:plugins '[ [lein-droid "0.2.3"] ] 
                                          :android {:sdk-path (if (= (nth sdk-path 0) \/)
                                                                  sdk-path
                                                                 (str "/home/" (:username (admin-user)) "/" sdk-path))}}}))))

(def lein-installation
  (server-spec
    :extends [(leiningen {})]
    :phases
      {:configure 
       (plan-fn) }))

(defplan adt-dependencies
  []
  (pallet.actions/exec-script
    (lib/install-package "lib32stdc++6")
    (lib/install-package "lib32z1")))

(defplan adt-shared-permissions
  [path]
  (directory (str path "/sdk") :mode "755" :recursive true)
  (directory (str path "/sdk/build-tools/android-4.4W") :mode "755" :recursive true)
  (directory (str path "/sdk/platforms/android-20") :mode "755" :recursive true)
  (pallet.actions/exec-script
   (lib/chmod "755" (str path "/sdk/tools/*"))
   (lib/chmod "755" (str path "/sdk/platform-tools/*"))
   (lib/chmod "755" (str path "/sdk/build-tools/android-4.4W/*"))))
  
(defplan adt-install
  [path & {shared :shared}]
  (with-action-options {:script-prefix (if shared :sudo :no-sudo)} 
    (remote-directory "/tmp"
                    :unpack :unzip
                    :mode "755"
                    :url "http://dl.google.com/android/adt/adt-bundle-linux-x86_64-20140702.zip")
    (exec-script (lib/mv "/tmp/adt-bundle-linux-x86_64-20140702" ~path :force true))
    (exec-script (lib/cp ~(str path "/sdk/build-tools/android-4.4W/zipalign") ~(str path "/sdk/tools/zipalign"))) ; for older build tools
    (if shared
      (adt-shared-permissions path)))
  (adt-dependencies)
  (extend-path "android-tools-env" (str path "/sdk/tools/:" path "/sdk/platform-tools/")))

(def shared-adt false)

(def
  ^{:doc "Define a server spec for droidbox"}
  droidbox-server
  (server-spec
   :extends [lein-installation]
   :phases
   {:configure (plan-fn
                 (adt-install (if shared-adt "/opt/android" "android") :shared shared-adt)
                 (lein-droid-deploy (if shared-adt "/opt/android/sdk" "android/sdk") )
                 (package-manager :update)
                 (package "maven")
                 (package "emacs")) }))

(def
  ^{:doc "Defines a group spec that can be passed to converge or lift."}
  droidbox
  (group-spec
   "droidbox"
   :extends [base-server missing-prerequisites java-server droidbox-server]
   :node-spec default-node-spec))




