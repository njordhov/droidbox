(ns droidbox.core.main
   (:require
    [droidbox.core.actions :refer [run]]))

(defn -main
  "Command-line entry point."
  [& raw-args]
  (run))