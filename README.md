Rsync for Tasker
================

An Android app providing Tasker plugin actions to allow running rsync over SSH.

[Tasker][1] is a very useful and widely used automation app for Android with a
plugin architecture that allows other apps (such as this one) to add extra
functionality.

This app provides the following actions for use within Tasker:

 - Generate Private Key - Generates a private key to use for SSH authentication
 - Get Public Key - Retrieves a public key from a previously generated private
   key and returns it in the variable `%pubkey`
 - rsync - Run an rsync command to synchronise files to or from the device
 - dbclient - Run an SSH command non-interactively (experimental)

The packaged native binaries of [Dropbear][2] and [rsync][3] are my own builds
which I keep updated with new releases.

Reasons for requested permissions:

 - `FOREGROUND_SERVICE` \
   Run actions for more than a short time when triggered from older versions of
   Tasker.
 - `FOREGROUND_SERVICE_SPECIAL_USE` \
   (Android 14+) Run actions for more than a short time when triggered from
   older versions of Tasker.
 - `INTERNET` \
   Allow connecting to SSH servers (even if the SSH server is on your local
   network Android prevents connections without this permission).
 - `MANAGE_EXTERNAL_STORAGE` \
   (Android 11+) To read or write the files on your device with rsync.
 - `WRITE_EXTERNAL_STORAGE` \
   (Android 10 and below) To read or write the files on your device with rsync.
 - `POST_NOTIFICATIONS` \
   Show a notification that a new version is available (if checking for updates
   is enabled).
 - `REQUEST_INSTALL_PACKAGES` \
   Prompt to install an update after you have tapped on the update notification
   and chosen 'Download'.

[1]: https://play.google.com/store/apps/details?id=net.dinglisch.android.taskerm
[2]: https://matt.ucc.asn.au/dropbear/dropbear.html
[3]: https://rsync.samba.org/


Download
--------

You can find a link to the latest download at
http://nerdoftheherd.com/projects/rsync-for-tasker/.


Licence
-------

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
