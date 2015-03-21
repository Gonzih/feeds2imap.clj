### 0.3.0

* changed format of cache to hmap
* added automatic cache cleanup functionality
* please remove your `~/.config/feeds2imap.clj/read-items.clj` if you experience any issues after update

### 0.2.2

* auto runs pull wrapped in catch statement in separate thread using futures and cancells previous task before launching new one.

### 0.2.1

* core.typed in main dependencies group.

### 0.2.0

* Rename `feeds2imap.feeds/digest` -> `feeds2imap.feeds/uniq-identifier`.
  First try to use `uri` or `url` or `link` as uniq identifier.
  If all of them are nil then calculate md5 of `(str title link authors)`.
