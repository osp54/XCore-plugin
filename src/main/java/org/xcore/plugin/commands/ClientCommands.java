package org.xcore.plugin.commands;

import arc.Events;
import arc.util.CommandHandler;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;

import static mindustry.Vars.mods;
import static org.xcore.plugin.XcorePlugin.*;

public class ClientCommands {
    public static void register(CommandHandler handler) {
        handler.<Player>register("js", "<code...>", "Execute javascript. [red]ADMIN ONLY", (args, player) -> {
            if (!player.admin) return;
            player.sendMessage("[green]"+mods.getScripts().runConsole(args[0]));
        });

        handler.<Player>register("rtv", "[off]", "Rock the vote to change map", (args, player) -> {
            if (player.admin()){
                rtvEnable = args.length != 1 || !args[0].equals("off");
            }
            if (!rtvEnable) {
                player.sendMessage("RTV: RockTheVote is disabled");
                return;
            }
            votes.add(player.uuid());
            int cur = votes.size();
            int req = (int) Math.ceil(ratio * Groups.player.size());
            Call.sendMessage("RTV: [accent]" + player.name + "[] wants to change the map, [green]" + cur +
                    "[] votes, [green]" + req + "[] required");

            if (cur < req) {
                return;
            }

            votes.clear();
            Call.sendMessage("RTV: [green] vote passed, changing map.");
            Events.fire(new EventType.GameOverEvent(Team.crux));
        });
    }
}
