package com.rundeck.plugin

import com.dtolabs.rundeck.core.common.INodeSet
import com.dtolabs.rundeck.core.plugins.Plugin
import com.dtolabs.rundeck.core.plugins.configuration.Description
import com.dtolabs.rundeck.core.resources.format.ResourceFormatGenerator
import com.dtolabs.rundeck.core.resources.format.ResourceFormatGeneratorException
import com.dtolabs.rundeck.plugins.ServiceNameConstants
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder
import com.fasterxml.jackson.databind.ObjectMapper

@Plugin(name = AnsibleResourceModelFormat.SERVICE_PROVIDER_TYPE, service = ServiceNameConstants.ResourceFormatGenerator)
class AnsibleResourceModelFormat implements ResourceFormatGenerator{
    public static final String SERVICE_PROVIDER_TYPE = "ansible-inventory";

    private static final Description DESCRIPTION = DescriptionBuilder.builder()
                                                                     .name(SERVICE_PROVIDER_TYPE)
                                                                     .title("Ansible Resource Formater")
                                                                     .description(
            "Transform rundeck resource node on ansible dynamic inventory format " +
            "(bundled)")
                                                                     .build()

    private final ObjectMapper test = new ObjectMapper();

    Description getDescription() {
        return DESCRIPTION;
    }

    @Override
    Set<String> getFileExtensions() {
        return Collections.unmodifiableSet(
                new HashSet<>(
                        Collections.singletonList(
                                "json"
                        )
                )
        )
    }

    @Override
    List<String> getMIMETypes() {
        return Collections.unmodifiableList(
                Arrays.asList(
                        "application/json", "text/json"
                )
        )
    }

    @Override
    void generateDocument(final INodeSet nodeset, final OutputStream stream)
            throws ResourceFormatGeneratorException, IOException {
        try {
            test.writeValue(stream, convertNodes(nodeset));
        } catch (IOException e) {
            throw new ResourceFormatGeneratorException(e);
        }
    }

    private Object convertNodes(final INodeSet nodeset) {
        HashMap<String, Map<String, String>> parent = new HashMap<>()
        HashMap<String, Map<String, String>> hostVars = new HashMap<>()
        HashMap<String, Map<String, String>> all = new HashMap<>()
        HashMap<String, Map<String, String>> output = new HashMap<>()

        def listNodes =[]
        def tags =[]
        def tagsHosts =[:]

        nodeset.each {node->
            HashMap<String, String> map = new HashMap<>()
            map.putAll(node.getAttributes())
            map.put("ansible_host",node.getHostname())
            if(node.getUsername()){
                map.put("ansible_user",node.getUsername())
            }

            if(node.osFamily.toLowerCase().contains("windows")){
                map.put("ansible_connection","winrm")
            }else{
                map.put("ansible_connection","ssh")
            }

            node.tags.each {tag->
                if(!tags.contains(tag)){
                    tags.add(tag)
                    tagsHosts.put(tag, [node.nodename])
                }else{
                    List nodesFromTag = tagsHosts.get(tag)
                    nodesFromTag.add(node.nodename)

                }
            }

            hostVars.put(node.getNodename(), map)

            if(!node.tags){
                listNodes.add(node.getNodename())
            }
        }

        all.put("children", tags)
        all.put("hosts",listNodes)
        parent.put("hostvars",hostVars)

        output.put("_meta",parent)
        output.put("rundeck",all)

        tagsHosts.each {tag,tagHosts ->
            def mapGroup = [:]
            mapGroup.put("hosts",tagHosts)
            output.put(tag,mapGroup)
        }
        return output
    }
}
