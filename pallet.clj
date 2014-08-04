;;; Pallet project configuration file

(require
 '[droidbox.groups.droidbox :refer [droidbox]])

(defproject droidbox
  :provider {:jclouds
             {:node-spec
              {:image {:os-family :ubuntu :os-version-matches "12.04"
                       :os-64-bit true}}}}

  :groups [droidbox])
