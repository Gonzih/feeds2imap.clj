### 0.2.1

* core.typed in main dependencies group.

### 0.2.0

* Rename `feeds2imap.feeds/digest` -> `feeds2imap.feeds/uniq-identifier`.
  First try to use `uri` or `url` or `link` as uniq identifier.
  If all of them are nil then calculate md5 of `(str title link authors)`.
