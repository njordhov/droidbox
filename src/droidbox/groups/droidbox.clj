(ns droidbox.groups.droidbox
    "Node defintions for droidbox"
    (:require
     [pallet.api :refer [group-spec server-spec node-spec plan-fn converge lift]]
     [pallet.crate.automated-admin-user :refer [automated-admin-user]]
     [pallet.actions :refer [package remote-directory remote-file directory exec-script]]
     [pallet.action :refer [with-action-options]]
     [pallet.script.lib :as lib :refer [config-root file user-home]]
     [pallet.stevedore :refer [script checked-script fragment]]
     [pallet.core.user :refer [*admin-user*]]
     [pallet.crate :refer [admin-user]]
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
                 (remote-directory "/opt"
                              :unpack :unzip
                              ; :owner (:username *admin-user*)
                              :url "http://dl.google.com/android/adt/adt-bundle-linux-x86_64-20140702.zip")
                 (exec-script (lib/mv "/opt/adt-bundle-linux-x86_64-20140702" "/opt/android" :force true))
                 (directory "/opt/android/sdk/tools" :mode "775")
                 (system-environment "android-tools" {"PATH" "/opt/android/sdk/tools/:/opt/android/sdk/platform-tools/:$PATH"})
                 ; likely redundant:
                 ; (lib/export "PATH" "~/android/sdk/tools/:~/android/sdk/platform-tools/:$PATH")
                 ;    (chown ~(:username (admin-user)) @tmpfile) :mode "755"
                 ; Make zipalign available for older build tools:
                 (exec-script (lib/cp "/opt/android/sdk/build-tools/android-4.4W/zipalign" "/opt/android/sdk/tools/zipalign"))
                 (with-action-options {:sudo-user (:username *admin-user*)}
                   (directory "~/.lein/")
                   ; (directory (str "/tmp/" (script (:username (admin-user)) "/.lein/")))
                   (remote-file "~/.lein/profiles.clj" :content
                               "{:user {:plugins [ [lein-droid \"0.2.3\"] ] :android {:sdk-path \"/opt/android/sdk\"}}}")
                  ))}))

(def
  ^{:doc "Defines a group spec that can be passed to converge or lift."}
  droidbox
  (group-spec
   "droidbox"
   :extends [base-server missing-prerequisites java-server droidbox-server]
   :node-spec default-node-spec))





