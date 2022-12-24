package org.xcore.plugin.commands;

import arc.util.CommandHandler;
import mindustry.gen.Player;

import static mindustry.Vars.mods;

public class ClientCommands {
    public static void register(CommandHandler handler) {
        handler.<Player>register("js", "<code...>", "Execute javascript. [red]ADMIN ONLY", (args, player) -> {
            if (!player.admin) return;
            player.sendMessage("[green]"+mods.getScripts().runConsole(args[0]));
        });
    }
}
