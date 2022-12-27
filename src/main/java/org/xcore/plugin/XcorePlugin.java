package org.xcore.plugin;

import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Strings;
import mindustry.gen.Call;
import mindustry.mod.Plugin;
import org.xcore.plugin.commands.ClientCommands;
import org.xcore.plugin.commands.ServerCommands;
import org.xcore.plugin.comp.Config;
import org.xcore.plugin.comp.ServersConfig;
import org.xcore.plugin.features.Console;

@SuppressWarnings("unused")
public class XcorePlugin extends Plugin {
    public static boolean isSocketServer;
    @Override
    public void init() {
        Config.load();
        Console.load();
        ServersConfig.load();
        Listeners.load();

        info("Plugin loaded");
    }
    @Override
    public void registerClientCommands(CommandHandler handler) {
        ClientCommands.register(handler);
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        ServerCommands.register(handler);
    }

    public static void info(String text, Object... values) {
        Log.infoTag("XCore", Strings.format(text, values));
    }
    public static void err(String text, Object... values) {
        Log.errTag("XCore", Strings.format(text, values));
    }
    public static void discord(String text, Object... values) {
        Log.infoTag("Discord", Strings.format(text, values));
    }
    public static void sendMessageFromDiscord(String authorName, String message) {
        discord("@: @", authorName, message);
        Call.sendMessage(Strings.format("[blue][Discord][] @: @", authorName, message));
    }
}