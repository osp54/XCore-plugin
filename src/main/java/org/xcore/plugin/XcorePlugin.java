package org.xcore.plugin;

import arc.Events;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Strings;
import fr.xpdustry.javelin.JavelinConfig;
import fr.xpdustry.javelin.JavelinEvent;
import fr.xpdustry.javelin.JavelinPlugin;
import fr.xpdustry.javelin.JavelinSocket;
import mindustry.game.EventType;
import mindustry.gen.Player;
import mindustry.mod.Plugin;
import org.xcore.plugin.commands.ClientCommands;
import org.xcore.plugin.commands.ServerCommands;
import org.xcore.plugin.comp.Config;
import org.xcore.plugin.discord.Bot;
import org.xcore.plugin.features.Console;

import static org.xcore.plugin.PluginVars.*;
import static mindustry.Vars.netServer;
import static org.xcore.plugin.discord.Bot.mainChannel;

@SuppressWarnings("unused")
public class XcorePlugin extends Plugin {
    @Override
    public void init() {
        Config.load();
        Console.load();

        Events.on(EventType.ServerLoadEvent.class, event -> {
            javelinSocket = JavelinPlugin.getJavelinSocket();
            if (JavelinPlugin.getJavelinConfig().getMode() == JavelinConfig.Mode.SERVER) {
                Bot.connect();

                javelinSocket.subscribe(XcorePlugin.MessageEvent.class, e -> {
                    XcorePlugin.info(e.getMessage());
                    mainChannel.sendMessage(
                            Strings.format("[@] @: @", e.getServer(), e.getAuthor().plainName(), e.getMessage())
                    ).queue();
                });
            }
        });

        Events.on(EventType.PlayerChatEvent.class, (event -> {
            javelinSocket.sendEvent(new MessageEvent(event.player, event.message, config.server));
        }));

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

    public static final class MessageEvent implements JavelinEvent {
        Player author;
        String message;
        String server;

        public MessageEvent(Player author, String message, String server) {
            this.author = author;
            this.message = message;
            this.server = server;
        }

        public Player getAuthor() {
            return author;
        }

        public String getMessage() {
            return message;
        }

        public String getServer() {
            return server;
        }
    }
}