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

## Enable DNS

Configure virtualbox to properly handle domain name resolution:

1. Open Machine:Settins from the VirtualBox menu.
2. Select the Network tab.
3. Adapter 1 should remain attached to "host-only".
3. Change the Attached To pop-up menu of Adapter 2 to "NAT". 
5. OK the dialog.

# Android App Development

Create a clone of the machine image called say droidbox-1:

    VBoxManage clonevm droidbox-0 --name droidbox-1 --register

Start up the new clone:

    VBoxManage startvm droidbox-1 --type gui

In the droidbox-1 window, press enter to select the *Ubuntu entry and continue until the login prompt.

Login from the prompt, or alternatively connect using:

    droidbox1=`VBoxManage guestproperty get "droidbox-1" "/VirtualBox/GuestInfo/Net/0/V4/IP" | awk '{ print $2 }'`
    ssh $droidbox1

##  Create the App

To create your first app see the tutorial at https://github.com/clojure-android/lein-droid/wiki/Tutorial

Create a new project using Android SDK 20:

    lein droid new droidapp com.droid :activity DroidActivity :target-sdk 20 :app-name CljDroid

Ignore the eventual warning about missing :android-dev profile.

If you use target-sdk 20 or later:

Optionally change the neko/neko dependency in project.clj to the latest version, e.g. ``3.1.0-beta1``

### Using an older target SDK

The target SDK has to be supported by the android running on the device. Target older versions of Android with earlier minimum SDKs. Use the [minimum SDK/API]](http://developer.android.com/about/dashboards/index.html) supported by the Android version on the devide.  

If you use a target-sdk below 20:

1. Open project.clj
2. Comment out the :javac-options
3. Change dependencies to

 
    :dependencies [[org.clojure-android/clojure "1.5.1-jb" :use-resources true]
                   [neko/neko "3.0.0-preview4"]]
                   
To list installed target SDKs:

    $ android list targets

The SDKs are installed in this directory:

    $ ls ~/android/sdk/platforms

Run this to install all SDKs (will take a while):

    $ android update sdk --all --no-ui

## Connect a Device

Connect your Android device via the USB of the host computer.

The device needs to be declared in Virtualbox to make it visible for your instance:

1. Open the Ports:USB Settings in the VirtualBox application. 
2. Use the + button to add devices.
3. Start up the instance again.

Verify that the Android device is connected:

    adb devices

Troubleshooting - if devices command outputs nothing or:

    ????????????	no permissions

then restart the adb server:

    sudo adb kill-server; sudo adb start-server; adb devices

You may have to answer a question on your mobile device.

## Build and Deploy on Device

For options see:

https://github.com/clojure-android/lein-droid
=======
If the device still isn't on the adb devices list, make sure the device in development mode:

1. Open Settings on the device.
2. Select the Developer Options item or equivalent.
3. Activate USB debugging.
4. Reconnect the USB cable to the device.
5. Select the device using the Devices:USB Devices menu of Virtualbox.

In the directory of the app:

    lein droid doall

After completion a basic app should be running on the device. 

To install and run a previous build on the device, execute:

    lein droid deploy -d

## Build and Deploy on Emulator

The brave can deploy to an android emulator on the host computer. The instructions below are rudimentary and still incomplete... see if you can get it to work.

You may have to first download the Android SDK on the host computer.

Start an emulator using the Android SDK Manager in the SDK distribution on the host computer:

    ./sdk/android

From the Tools menu of the Android SDK Manager, start the Android Virtual Device (AVD) manager by selecting the item labeled Manage AVDs. Start your preferred virtual device.

See the section on Emulator Networking in the documentation for the Android emulator:
https://developer.android.com/tools/devices/emulator.html#emulatornetworking

"The emulator listens for connections on ports 5554-5587 and accepts connections only from localhost."

See also:
http://www.deadcodersociety.org/blog/forwarding-a-range-of-ports-in-virtualbox/

On the host machine, execute the following to make the first emulator available to the virtual box: 

    VBoxManage modifyvm "droidbox-1" --natpf1 "guestssh,tcp,,5554,,5554"
    VBoxManage modifyvm "droidbox-1" --natpf1 "guestssh,tcp,,5555,,5555"

You may have to stop the virtual machine before running the command. Use the post number shown on the top of the emulator window in place of 5554. 

Alternatively use the post mapping on the virtual machine manager application:

1. Turn off the instance.
2. Open the Settings
3. Select the Network tab.
4. Select an unused Adapter (from the subtabs).
5. Set it to be Attached to NAT.
7. Create a new Rule Using the + button to the right.
7. Open the Advanced options and click Port Forwarding.
8. Type in 5554 as Host Port and the same as Guest Port.
8. Repeat the last steps to add a similar rule for port 5555.

You can list the available emulators and devices by running this on the virtual instance:

    adb devices

Install a previous build by executing the install command on the target virtual instance:

    lein droid deploy -e

# Customization

You can create your own droidbox variation (which may extend an existing or new droidbox image). 
Assuming you have a running droidbox instance you can interact with it from the repl:

    $ lein repl

In the repl:

    (use 'pallet.repl)
    (require 'pallet.actions 'pallet.api 'pallet.core.user 'pallet.compute)
    (def vmfest (pallet.compute/instantiate-provider "vmfest"))
    (show-nodes vmfest)
    (def my-droidbox
        (pallet.api/group-spec (str "my-droidbox" (System/currentTimeMillis))
           :phases {:configure
                     (pallet.api/plan-fn
                       (pallet.actions/exec-script ("ls"))) }))
    (pallet.api/lift my-droidbox :compute vmfest :user pallet.core.user/*admin-user*)


Copyright ©2014-2015 Terje Norderhaug

Distributed under the Eclipse Public License.
