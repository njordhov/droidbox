DroidBox
========
by Terje Norderhaug

VirtualBox development environment/sandbox for building Android apps in Clojure using the current SDK.

Version: Preview

Installs a canonical Clojure Android app development sandbox on VirtualBox:

* Ubuntu 14.04
* Oracle JDK7
* Leiningen (latest stable version)
* Lein Droid 0.2.3
* Android SDK Tools 23.0.2
* Maven 
* emacs

## Prerequisites

Install [VirtualBox 4.3.x](https://www.virtualbox.org/wiki/Downloads) or later.

Review and accept the Android SDK license at http://developer.android.com/sdk/index.html

Clone this repo from git.

The instructions below assumes OSX. See the [vmfest README](https://github.com/tbatchelli/vmfest) for platform-specific requirements.

## Create VM Instance

From the root of this project, execute:

    lein run

This will install a canonical VM environment for Android development. Expect it to take a while. 
If the run fails (possibly due to network problems) then start it again.

#### Alternative Manual Run *

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

## Activate USB

Activate USB from a host computer terminal:

    VBoxManage controlvm droidbox-0 poweroff
    VBoxManage modifyvm droidbox-0  --usb on --usbehci on

Alternatively, manually edit the Settings in the VirtualBox application after powering down the instance. 
See the Ports:USB tab, Enable USB Controller and USB 2.0.

# Android App Development

Create a clone of the machine image called say droidbox-1:

    VBoxManage clonevm droidbox-0 --name droidbox-1 --register

Start up the new clone:

    VBoxManage startvm droidbox-1 --type gui

In the droidbox-1 window, press enter to select the *Ubuntu entry and continue to login.

Connect using:

    droidbox1=`VBoxManage guestproperty get "droidbox-1" "/VirtualBox/GuestInfo/Net/0/V4/IP" | awk '{ print $2 }'`
    ssh $droidbox1

##  Create the App

To create your first app see the tutorial at https://github.com/clojure-android/lein-droid/wiki/Tutorial

Create a new project using Android SDK 20:

    lein droid new droidapp com.droid :activity DroidActivity :target-sdk 20 :app-name CljDroid

Ignore the eventual warning about missing :android-dev profile.

If you use target-sdk 20 or later:

Optionally change the neko/neko dependency in project.clj to the latest version, e.g. ``3.1.0-beta1``

If you use a target-sdk below 20:

1. Open project.clj
2. Comment out the :javac-options
3. Change dependencies to

 
    :dependencies [[org.clojure-android/clojure "1.5.1-jb" :use-resources true]
                   [neko/neko "3.0.0-preview4"]]

## Connect a Device

Connect your Android device via the USB of the host computer.

The device needs to be declared in Virtualbox to make it visible for your instance:

1. Open the Ports:USB Settings in the VirtualBox application. 
2. Use the + button to add devices.
3. Start up the instance again.

    cd ~

Verify that the Android device is connected:

    adb devices

Troubleshooting - if devices command outputs nothing or:

    ????????????	no permissions

then restart the adb server:

    sudo adb kill-server; sudo adb start-server; adb devices

You may have to answer a question on your mobile device.

If the device still isn't on the adb devices list, make sure the device in development mode:

1. Open Settings on the device.
2. Select the Developer Options item or equivalent.
3. Activate USB debugging.
4. Reconnect the USB cable to the device.
5. Select the device using the Devices:USB Devices menu of Virtualbox.

## Build and Deploy

In the directory of the app:

    lein droid doall

After completion a basic app should be running on the device. For more options see:

https://github.com/clojure-android/lein-droid


Copyright ©2014 Terje Norderhaug

Distributed under the Eclipse Public License.
