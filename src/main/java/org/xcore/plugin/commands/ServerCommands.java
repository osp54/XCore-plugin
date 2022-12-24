package org.xcore.plugin.commands;

import arc.util.CommandHandler;
import org.xcore.plugin.comp.Config;

import static org.xcore.plugin.PluginVars.*;
public class ServerCommands {
    public static void register(CommandHandler handler) {
        handler.register("reload-config", "Reload config", args -> {
            config = gson.fromJson(configFile.reader(), Config.class);
        });
    }
}
