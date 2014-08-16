DroidBox
========
by Terje Norderhaug

Virtual development environment/sandbox for building Android apps in Clojure

Follow these steps and you're good to go with a Clojure Android app development sandbox on VirtualBox.

## Prerequisites

Install [VirtualBox 4.3.x](https://www.virtualbox.org/wiki/Downloads) or later.

Review and accept the Android SDK license at http://developer.android.com/sdk/index.html

Clone this repo from git.

The instructions below assumes OSX. See the [vmfest README][vmfest] for platform-specific requirements.

## Create VM Instance

To build a new sandbox development environment, from the root of this project, execute:

    lein run

This will download Ubuntu (if required), install it on a new instance, then start the instance. This may take some time.

### Alternative Manual Run

Open a repl for this project:

    lein repl

In the repl, start a new VM instance:

    (use 'droidbox.core.actions)
    (def s (run))

This will download Ubuntu (if required), install it on a new instance, then start the instance.
Take a break while you wait: It will likely take quite a bit of time.

Note the IP address shown for the new image.

Troubleshooting if spinning up fails, evaluate:

    (use 'pallet.repl)
    (explain-session s)

## Connecting to the Instance

Connect using:

    ssh 192.168.56.1

Use the ip adress from the install in place of 192.168.56.1

## Final Steps

Save the image in VirtualBox.
Consider creating a clone of this image for new instances to avoid repeating the steps each time.

# App Development

To create your first app see the tutorial at https://github.com/clojure-android/lein-droid/wiki/Tutorial

With the configuration above you are ready to use Android SDK 20 as in:

    lein droid new droidapp com.droid :activity DroidActivity :target-sdk 20 :app-name CljDroid

If you use target-sdk 20 or later:

Optionally change the neko/neko dependency to the latest version, e.g. ``3.1.0-beta1``

If you use a target-sdk below 20:

1. Open project.clj
2. Comment out the :javac-options
3. Change dependencies to:
 
    :dependencies [[org.clojure-android/clojure "1.5.1-jb" :use-resources true]
                   [neko/neko "3.0.0-preview4"]]

## Connect a Device

Devices can be connected via the USB of the host computer. 

First activate USB on the VM, for example by editing the Settings in the VirtualBox after shutting down the instance. 
See the Ports:USB tab, Enable USB Controller and USB 2.0, then use the + button to add your devices. 

Verify that the device is connected:

    adb devices

Troubleshooting - if devices command outputs:

    ????????????	no permissions

then restart the adb server:

    sudo adb kill-server; sudo adb start-server

You may have to answer a question on your mobile device.

## Build and Deploy

In the directory of the app:

    lein droid doall

A basic app should now be running on the device. For more options see:

https://github.com/clojure-android/lein-droid

Troubleshooting - if Creating Dex is aborted on lein droid build...
consider increase the RAM for the VM (512 is too little, 1024 works for plain ubuntu 14.04 with no gui)


Copyright ©2014 Terje Norderhaug

Distributed under the Eclipse Public License.
