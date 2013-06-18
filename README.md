## feeds2imap.clj

RSS/Atom reader implemented in Clojure.
It stores new items in the mail folders using IMAP APPEND command.

### Configuration
By default configuration dir is `$HOME/.config/feeds2imap.clj`.

Only file than required to run code is imap.clj. Take a look at imap.clj.example file for details.

### Usage
You can use `lein run` to run programm or you can generate jar with `lein uberjar` and run programm with `java -jar <path-to-jar>`.

* `LAUNCH-COMMAND` - pull new items.
* `LAUNCH-COMMAND pull` - pull new items.
* `LAUNCH-COMMAND auto` - pull new items every 1 hour in the loop.
* `LAUNCH-COMMAND show` - show feeds list (url.clj file content).
* `LAUNCH-COMMAND add folder-name feed-url` - add url to feeds file to the folder folder-name.

where `LAUNCH-COMMAND = lein run / lein trampoline run / java -jar jarfile.jar`.

### License

Copyright © 2013 Max Gonzih gonzih at gmail.com

Distributed under the Eclipse Public License, the same as Clojure.

### Thanks
Thanks to Greg Hendershott for original idea implemented in Racket
http://www.greghendershott.com/2013/05/feeds2gmail.html

### Similar project
* https://github.com/Gonzih/feeds2imap.rb - My implementation in Ruby.
* http://www.greghendershott.com/2013/05/feeds2gmail.html - Racket implementation by Greg Hendershott.
* https://github.com/cordawyn/rss2imap - Haskell implementation by Slava Kravchenko.
