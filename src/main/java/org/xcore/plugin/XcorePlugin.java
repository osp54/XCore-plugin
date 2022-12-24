package org.xcore.plugin;

import arc.util.CommandHandler;
import arc.util.Log;
import mindustry.mod.Plugin;
import org.xcore.plugin.commands.ClientCommands;
import org.xcore.plugin.commands.ServerCommands;
import org.xcore.plugin.comp.Config;
import org.xcore.plugin.features.Console;

@SuppressWarnings("unused")
public class XcorePlugin extends Plugin {
    @Override
    public void init() {
        Config.load();
        Console.load();

        Log.infoTag("XCore", "Plugin loaded");
    }
    @Override
    public void registerClientCommands(CommandHandler handler) {
        ClientCommands.register(handler);
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        ServerCommands.register(handler);
    }

    public static void info(String text) {
        Log.infoTag("XCore", text);
    }
    public static void err(String text) {
        Log.errTag("XCore", text);
    }
}