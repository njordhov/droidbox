
(ns droidbox.core.main
   (:require
    [droidbox.groups.droidbox :refer [spin]]))

(defn -main
  "Command-line entry point."
  [& raw-args]
  (spin))