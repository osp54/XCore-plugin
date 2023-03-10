package org.xcore.plugin;

import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Reflect;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.game.Gamemode;
import mindustry.gen.AdminRequestCallPacket;
import mindustry.gen.Call;
import mindustry.gen.Iconc;
import mindustry.maps.Map;
import mindustry.maps.Maps.MapProvider;
import mindustry.mod.Plugin;
import mindustry.net.Packets;
import org.xcore.plugin.commands.ClientCommands;
import org.xcore.plugin.commands.ServerCommands;
import org.xcore.plugin.listeners.NetEvents;
import org.xcore.plugin.listeners.PluginEvents;
import org.xcore.plugin.modules.*;
import org.xcore.plugin.modules.hexed.MiniHexed;
import org.xcore.plugin.utils.Config;
import org.xcore.plugin.utils.Database;
import org.xcore.plugin.utils.GlobalConfig;

import static mindustry.Vars.maps;
import static mindustry.Vars.netServer;
import static org.xcore.plugin.utils.Utils.getAvailableMaps;

@SuppressWarnings("unused")
public class XcorePlugin extends Plugin {

    public XcorePlugin() {
        Config.init();
        GlobalConfig.init();
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

    @Override
    public void init() {
        Database.init();
        Console.init();
        MiniPvP.init();
        MiniHexed.init();
        LastStanding.init();
        PluginEvents.init();
        AdminModIntegration.init();
        Translator.init();
        maps.setMapProvider(new MapProvider() {
            public int lastMapID;

            @Override
            public Map next(Gamemode mode, Map previous) {
                var allmaps = getAvailableMaps();
                return allmaps.any() ? allmaps.get(lastMapID++ % allmaps.size) : null;
            }
        });
        netServer.admins.addChatFilter(NetEvents::chat);
        Vars.net.handleServer(AdminRequestCallPacket.class, NetEvents::adminRequest);
        Vars.net.handleServer(Packets.Connect.class, NetEvents::connect);
        Vars.net.handleServer(Packets.ConnectPacket.class, NetEvents::connectPacket);

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
}