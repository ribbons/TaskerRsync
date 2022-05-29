Rsync for Tasker
================

![CI status](https://github.com/ribbons/TaskerRsync/workflows/CI/badge.svg)

An Android app providing Tasker plugin actions to allow running rsync over SSH.

[Tasker](1) is a very useful and widely used automation app for Android with a
plugin architecture that allows other apps (such as this one) to add extra
functionality.

This app provides the following actions for use within Tasker:
 - Generate Private Key - Generates a private key to use for SSH authentication
 - Get Public Key - Retrieves a public key from a previously generated private key
 - rsync - Run an rsync command to synchronise files to or from the device
 - dbclient - Run an SSH command non-interactively (experimental)

The packaged native binaries of [Dropbear](2) and [rsync](3) are my own builds
which I keep updated with new releases.

[1]: https://play.google.com/store/apps/details?id=net.dinglisch.android.taskerm
[2]: https://matt.ucc.asn.au/dropbear/dropbear.html
[3]: https://rsync.samba.org/


Download
--------

You can find a link to the latest download at
http://nerdoftheherd.com/projects/rsync-for-tasker/.
