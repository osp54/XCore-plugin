package org.xcore.plugin;

import arc.Events;
import arc.util.Strings;
import fr.xpdustry.javelin.JavelinConfig;
import fr.xpdustry.javelin.JavelinPlugin;
import mindustry.game.EventType;
import org.xcore.plugin.discord.Bot;

import static org.xcore.plugin.PluginVars.config;
import static org.xcore.plugin.XcorePlugin.isSocketServer;

public class Listeners {
    public static void load() {
        Events.on(EventType.ServerLoadEvent.class, event -> {
            isSocketServer = JavelinPlugin.getJavelinConfig().getMode() == JavelinConfig.Mode.SERVER;
            if (isSocketServer) {
                Bot.connect();

                JavelinPlugin.getJavelinSocket().subscribe(SocketEvents.MessageEvent.class, e -> {
                    Bot.sendMessageEventMessage(e.authorName, e.message, e.server);
                });

                JavelinPlugin.getJavelinSocket().subscribe(SocketEvents.PlayerJoinLeaveEvent.class, e -> {
                    Bot.sendJoinLeaveEventMessage(e.playerName, e.server, e.join);
                });
            }
        });

        Events.on(EventType.PlayerChatEvent.class, (event -> {
            if (!Bot.isConnected) return;
            if (isSocketServer) {
                Bot.getServerLogChannel().sendMessage(
                        Strings.format("`@: @`", event.player.plainName(), event.message)
                ).queue();
            } else {
                JavelinPlugin.getJavelinSocket().sendEvent(
                        new SocketEvents.MessageEvent(event.player.plainName(), event.message, config.server));
            }
        }));

        Events.on(EventType.PlayerJoin.class, event -> {
            if (!Bot.isConnected) return;
            if (isSocketServer) {
                Bot.sendJoinLeaveEventMessage(event.player.plainName(), true);
            } else {
                JavelinPlugin.getJavelinSocket().sendEvent(
                        new SocketEvents.PlayerJoinLeaveEvent(event.player.plainName(), config.server, true)
                );
            }
        });

        Events.on(EventType.PlayerLeave.class, event -> {
            if (!Bot.isConnected) return;
            if (isSocketServer) {
                Bot.sendJoinLeaveEventMessage(event.player.plainName(), false);
            } else {
                JavelinPlugin.getJavelinSocket().sendEvent(
                        new SocketEvents.PlayerJoinLeaveEvent(event.player.plainName(), config.server, false)
                );
            }
        });
    }
}
