# Deprecated

This repository is no longer maintained and is only avaiable for historical example purposes.  It will be deleted at some point in the future.

# Ansible Resource Format

This plugin transforms a set of Node resources into an [Ansible dynamic inventory format](https://docs.ansible.com/ansible/latest/user_guide/intro_dynamic_inventory.html) 

## Build and install

To build the plugin use:

```
gradle clean build
```

To install, copy the jar file to rundeck:

```
cp build/libs/ansible-resource-format-X.X.X.jar $RDECK_BASE/libext

```

## How to use

* On the resource API URL , add the parameter `format` with the value `ansible-inventory`. For example: 

```
http://rundeck:4440/api/26/project/<Project>/resources?authtoken=<Token>&format=ansible-inventory
```

* create a python file to add the dynamic inventory, see an example on `example/rundeck-inventory.py`

* test the dynamic inventory:

```
python example/rundeck-inventory.py
```

You should see something like:

```
{
  "_meta": {
    "hostvars": {
      "localhost": {
        "ansible_connection": "ssh",
        "ansible_host": "localhost",
        "description": "Rundeck server node",
        "hostname": "localhost",
        "nodename": "localhost",
        "osArch": "x86_64",
        "osFamily": "unix",
        "osName": "Mac OS X",
        "osVersion": "10.13.6"
      },
      "remotenode": {
        "ansible_connection": "ssh",
        "ansible_host": "192.168.0.13",
        "ansible_user": "rundeck",
        "custom:test": "This is a test",
        "hostname": "192.168.0.13",
        "nodename": "remotenode",
        "osFamily": "Centos",
        "osName": "Centos7",
        "osType": "X86-64",
        "username": "rundeck"
      }
    }
  },
  "rundeck": {
    "children": [
       "centos7"
    ],
    "hosts": [
      "localhost"
    ]
  },
  "centos7": {
      "hosts": [
        "remotenode"
      ]
  }
}
```

Rundeck tags are transform on Ansible groups.

* Use the Ansible inventory:

```
ansible -i rundeck-inventory.py all -m ping
```

or filtering by tag

```
ansible -i rundeck-inventory.py centos7 -m ping
```
