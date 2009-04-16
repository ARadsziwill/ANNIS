#!/usr/bin/python

import os, sys

#conf
path_to_app=sys.argv[1]
rev=sys.argv[2]
user_conf_dir=sys.argv[3]

#code
config_js_path=path_to_app + "/javascript/annis/config.js"
print "patching " + config_js_path  + " (with revision number: " + rev + ")" 
fJS = open(config_js_path, "r")
JSContent = fJS.read();
fJS.close();

fJS = open(config_js_path, "w")
fJS.write(JSContent.replace("${SVN_REVISION}", rev))
fJS.close()

web_xml_path=path_to_app + "/WEB-INF/web.xml"
print "patching " + web_xml_path  + " (user-conf-dir: " + user_conf_dir + ")" 
lineWasFound = False
fWeb = open(web_xml_path, 'r')
WebContent = fWeb.read()
fWeb.close()

fWeb = open(web_xml_path, 'w')
fWeb.write(WebContent.replace("<param-value>/etc/annis/user_config_dev/</param-value>", "<param-value>%s</param-value>\n" % user_conf_dir))
fWeb.close()
