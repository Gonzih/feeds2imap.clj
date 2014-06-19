version="0.2.3"
deploy_host="beaglebone"

default:
	lein with-profile dev trampoline midje
	lein with-profile production uberjar
	scp "target/feeds2imap-$(version)-standalone.jar" $(deploy_host):mydev/feeds2imap.clj/
	ssh $(deploy_host) "echo $(version) > ~/mydev/feeds2imap.clj/version"
