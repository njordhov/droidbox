(ns droidbox.core.main
   (:require
    [droidbox.core.actions :refer [run install-ubuntu]]))

(defn -main
  "Command-line entry point."
  [& raw-args]
  (install-ubuntu)
  (run))