(ns droidbox.core.main
   (:require
    [droidbox.core.actions :refer [spin install-ubuntu]]))

(defn -main
  "Command-line entry point."
  [& raw-args]
  (install-ubuntu)
  (spin))