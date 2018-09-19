#!/usr/bin/env python

import json
import urllib2

url="http://rundeck:4440/api/26"
token="sometoken"
project="ProjectName"

data = json.load(urllib2.urlopen(url + '/project/'+project+'/resources?authtoken='+token+'&format=ansible-inventory'))
print(json.dumps(data, sort_keys=True, indent=2))