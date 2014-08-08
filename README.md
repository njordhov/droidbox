DroidBox
========
by Terje Norderhaug

Virtual development environment/sandbox for building Android apps in Clojure

Follow these steps and you're good to go with a Clojure Android app development sandbox on VirtualBox.

## Prerequisites

Install [VirtualBox 4.3.x](https://www.virtualbox.org/wiki/Downloads) or later.

The instructions below assumes OSX. See the [vmfest][vmfest] README for platform-specific requirements.

## Create VM Instance

To build a new sandbox development environment, from the root of this project, execute:

    lein run

This will download Ubuntu (if required), install it on a new instance, then start the instance. This may take some time.

### Alternative Manual Run

Open a repl for this project:

    lein repl

In the repl, start a new VM instance:

    (use 'droidbox.groups.droidbox)
    (def s (spin))

This will download Ubuntu (if required), install it on a new instance, then start the instance.
Take a break while you wait, expect to wait for a bit.

Note the IP address shown for the new image.

Troubleshooting if spinning up fails, evaluate:

    (use 'pallet.repl)
    (explain-session s)

## Connecting to the Instance

Connect to the new instance using:

    ssh 192.168.1.1

Use the ip adress of the instance in place of 192.168.1.1

## Install Emacs

Alternatively your editor of choice.

    sudo apt-get install emacs ; sudo apt-get update

## Install Maven

    sudo apt-get install maven

## Android CLJ

    mkdir ~/.lein/
    emacs ~/.lein/profiles.clj

Change the content of ~/.lein/profiles.clj as follows:

    {:user {:plugins [ [lein-droid "0.2.3"] ] :android {:sdk-path "~/android/sdk"}}}

Troubleshooting:
if the profiles.clj doesn't seem to take effect (so no droid task) consider installing it manually:

    lein plugin install lein-droid "0.2.3" 

## Generate Key

Generate a key for releases: 

    mkdir ~/.android/
    keytool -genkey -v -keystore ~/.android/debug.keystore -storepass android -alias androiddebugkey -keypass android -dname "CN=Android Debug,O=Android,C=US" -keyalg RSA -keysize 2048

Later you can use the path to the key in the profile.clj file.

## Final Steps

Save the image in VirtualBox.
Create a clone of this image for new instances to avoid repeating the steps each time.

# App Development

To create your first app see the tutorial at https://github.com/clojure-android/lein-droid/wiki/Tutorial

With the configuration above you are ready to use Android SDK 20 as in:

    lein droid new droidapp com.droid :activity DroidActivity :target-sdk 20 :app-name CljDroid

If you use a target-sdk below 20:

1. Open project.clj
2. Comment out the :javac-options
3. Change dependencies to:
 
    :dependencies [[org.clojure-android/clojure "1.5.1-jb" :use-resources true]
                   [neko/neko "3.0.0-preview4"]]

If you use target-sdk 20 or later, consider changing the neko/neko dependency to the latest version, e.g. ``3.1.0-beta1``

## Connect a Device

Devices can be connected via the USB of the host computer. 

Verify that the device is available:

    adb devices

Troubleshooting if devices command outputs

    ????????????	no permissions

Then restart the adb server:

    sudo adb kill-server; sudo adb start-server

You may have to answer a question on your mobile device.

## Build and Deploy

In the directory of the app:

    lein droid doall

A basic app should now be running on the device.

Troubleshooting:
If Creating Dex is aborted on lein droid build...
consider increase the RAM for the VM (512 is too little, 1024 works for plain ubuntu 14.04 with no gui)

Troubleshooting:
when it says zipalign doesn't exist...
See instructions above to make zipalign available as a tool.

Troubleshooting if leiningen isn't yet installed, execute:
install leiningen as instructed above.

Troubleshooting if can't run aatp: 
install 32bit as specified earlier on.



Copyright ©2014 Terje Norderhaug

Distributed under the Eclipse Public License.
