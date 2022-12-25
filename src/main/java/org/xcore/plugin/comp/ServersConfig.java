package org.xcore.plugin.comp;

import arc.util.serialization.Jval;
import java.util.HashMap;

import static org.xcore.plugin.PluginVars.serversConfigFile;

public class ServersConfig {
    public static HashMap<String, Long> servers= new HashMap<>();
    public static void load() {
        Jval.read(serversConfigFile.reader()).asObject().forEach(jval -> {
            servers.put(jval.key, jval.value.asLong());
        });
    }
}
