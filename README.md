## feeds2imap.clj [![License](http://img.shields.io/badge/license-EPL-blue.svg?style=flat)](https://www.eclipse.org/legal/epl-v10.html) [![Build Status](https://travis-ci.org/Gonzih/feeds2imap.clj.svg?branch=master)](https://travis-ci.org/Gonzih/feeds2imap.clj) [![Dependencies Status](http://jarkeeper.com/Gonzih/feeds2imap.clj/status.svg)](https://jarkeeper.com/Gonzih/feeds2imap.clj)

[![Clojars Project](http://clojars.org/feeds2imap/latest-version.svg)](http://clojars.org/feeds2imap)

![clojure feeds reader](https://raw.githubusercontent.com/Gonzih/feeds2imap.clj/master/demo.gif)

RSS/Atom reader implemented in Clojure.
It stores new items in the mail folders using IMAP APPEND command (java.mail framework).

### Configuration
By default configuration dir is `$HOME/.config/feeds2imap.clj`.
You can change it setting environment variable `$FEEDS2IMAP_HOME` (something like `FEEDS2IMAP_HOME=/home/me/.someotherdir lein ...`).

The only file required to run the code is `imap.clj`. Take a look at `imap.clj.example` for details.

Feeds and folders can be added using the `add` command (see below), or you can write them directly in the `urls.clj` file (inside the configuration directory).
The format of this file is as follows:

```clojure
{:folder1 ["url1" "url2" ...]
 :folder2 [...]
 ...}
```

### Usage
You can use [lein plugin](https://github.com/Gonzih/lein-feeds2imap).

Or you can use `lein run` to run programm or you can generate jar with `lein uberjar` and run programm with `java -jar <path-to-jar>`.

* `LAUNCH-COMMAND` - pull new items.
* `LAUNCH-COMMAND pull` - pull new items.
* `LAUNCH-COMMAND auto` - pull new items every 1 hour in the loop.
* `LAUNCH-COMMAND show` - show feeds list (url.clj file content).
* `LAUNCH-COMMAND add folder-name feed-url` - add url to feeds file to the folder folder-name.
* `LAUNCH-COMMAND imap encrypt` - encrypt imap.clj file using gpg
* `LAUNCH-COMMAND imap decrypt` - decrypt imap.clj file using gpg
* `LAUNCH-COMMAND opml2clj filename.xml [path/to/urls.clj]` - convert OPML file to `urls.clj` format

where `LAUNCH-COMMAND = lein run / lein trampoline run / java -jar jarfile.jar`.

### Sample systemd user service (~/.config/systemd/user)

```
[Unit]
Description=Clojure feeds reader

[Service]
ExecStart=java -jar feeds2imap.jar auto

[Install]
WantedBy=default.target
```

```sh
systemctl --user enable feeds2imap.service
systemctl --user start feeds2imap.service
```

All service output will be collected by journald.


### License

Copyright © 2013-2015 Max Gonzih gonzih @ gmail.com

Distributed under the Eclipse Public License, the same as Clojure.

### Thanks
Thanks to Greg Hendershott for original idea implemented in Racket
http://www.greghendershott.com/2013/05/feeds2gmail.html

### Similar projects

* [My Ruby prototype](https://github.com/Gonzih/feeds2imap.rb)
* [My Clojure implementation](https://github.com/Gonzih/feeds2imap.clj)
* [Racket implementation](https://github.com/greghendershott/feeds2gmail)
* [Haskell implementation](https://github.com/cordawyn/rss2imap)
