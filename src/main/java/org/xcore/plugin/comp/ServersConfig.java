package org.xcore.plugin.comp;

import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.serialization.Jval;

import static org.xcore.plugin.PluginVars.serversConfigFile;

public class ServersConfig {
    public static ObjectMap<String, Long> servers= new ObjectMap<>();
    public static void load() {
        Jval.read(serversConfigFile.reader()).asObject().forEach(jval -> {
            servers.put(jval.key, jval.value.asLong());
        });
        Log.info(servers.toString());
    }
}
