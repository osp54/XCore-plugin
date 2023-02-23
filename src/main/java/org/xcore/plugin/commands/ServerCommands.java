package org.xcore.plugin.commands;

import arc.func.Cons2;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Time;
import com.mongodb.client.result.DeleteResult;
import mindustry.gen.Groups;
import mindustry.net.Administration.PlayerInfo;
import mindustry.net.Packets;
import org.xcore.plugin.modules.Config;
import org.xcore.plugin.modules.Database;
import org.xcore.plugin.modules.GlobalConfig;
import org.xcore.plugin.modules.models.BanData;
import org.xcore.plugin.modules.models.PlayerData;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import static arc.util.Strings.parseInt;
import static java.lang.Long.parseLong;
import static mindustry.Vars.netServer;
import static org.xcore.plugin.PluginVars.config;
import static org.xcore.plugin.Utils.temporaryBan;

public class ServerCommands {
    public static void register(CommandHandler handler) {
        handler.register("reload-config", "Reload config", args -> {
            Config.init();
            GlobalConfig.init();
        });
        handler.register("edit-rating", "<uuid> <+/-/value> [hex/pvp]", "Edit player`s rating.", args -> {
            PlayerInfo info = netServer.admins.getInfoOptional(args[0]);
            char operator = args[1].charAt(0);

            boolean pvp;
            if (args.length >= 3) {
                pvp = args[2].equals("pvp");
            } else {
                pvp = true;
            }

            if (info == null) {
                Log.info("Player not found.");
                return;
            }

            PlayerData data = Database.getPlayerData(info.id);

            if (!data.exists) {
                Log.err("Player in db not found.");
                return;
            }

            Cons2<Integer, Boolean> set = (i, b) -> {
                if (pvp) {
                    data.pvpRating = b ? i : data.pvpRating + i;
                } else {
                    data.hexedWins += b ? i : data.hexedWins + i;
                }
            };

            switch (operator) {
                case '+' -> {
                    int increment = parseInt(args[1].substring(1));
                    set.get(increment, false);
                }
                case '-' -> {
                    int reduce = parseInt(args[1].substring(1));
                    set.get(-reduce, false);
                }
                default -> set.get(parseInt(args[1]), true);
            }

            Database.setPlayerData(data);
            Log.info("'@' rating is now @(pvp), @(hex)", data.nickname, data.pvpRating, data.hexedWins);
        });

        handler.register("dbinfo", "<uuid>", "Info about player from db.", args -> {
            PlayerInfo info = netServer.admins.getInfoOptional(args[0]);

            if (info == null) {
                Log.info("Player not found.");
                return;
            }

            PlayerData data = Database.getPlayerData(info.id);

            if (!data.exists) {
                Log.err("Player in db not found.");
                return;
            }

            Log.info("'@' DB '@': ", info.plainLastName(), data.nickname);
            Log.info("  PvP Rating: @", data.pvpRating);
            Log.info("  Hexed Wins: @", data.hexedWins);
            Log.info("  Translator Language: @", data.translatorLanguage);
        });

        handler.register("tempban", "<uuid> <days-of-ban> <reason...>", "Temporary ban player.", args -> {
            var target = netServer.admins.getInfoOptional(args[0]);

            if (target == null) {
                Log.err("Player not found.");
                return;
            }

            long days;
            try {
                days = parseLong(args[1]);
            } catch (NumberFormatException ignored) {
                Log.err("Ban days must be a number.");
                return;
            }

            if (days <= 0) {
                Log.err("Ban days must be a positive number.");
                return;
            }

            Groups.player.each(p -> p.uuid().equals(target.id) || p.ip().equals(target.lastIP), p -> p.kick(Packets.KickReason.banned));

            temporaryBan(new BanData(target.id, target.lastIP, target.lastName, "console", args[2], config.server, Time.millis() + TimeUnit.DAYS.toMillis(days)));
        });

        handler.register("tempbans", "List all temporary banned players.", args -> {
            Log.info("Temporary banned players:");
            Seq<BanData> bans = Database.getBanned();

            bans.each(ban -> {
                var info = netServer.admins.getInfoOptional(ban.uuid);

                var date = LocalDateTime.ofInstant(Instant.ofEpochMilli(ban.unbanDate), ZoneId.systemDefault()).toString();

                if (info != null) {
                    Log.info(
                            "  '@' / Last known name: '@' / IP: '@' / Unban date: @ / Reason: '@'",
                            ban.uuid,
                            info.plainLastName(),
                            ban.ip,
                            date,
                            ban.reason);
                } else {
                    Log.info("  '@' / IP: '@' / Unban date: @ / Reason: '@'", ban.uuid, ban.ip, date, ban.reason);
                }
            });
        });

        handler.register("tempunban", "<uuid/ip>", "Unban a temporary banned player.", args -> {
            DeleteResult result = Database.unBan(args[0], "");
            if (result.getDeletedCount() < 1) Database.unBan("", args[0]);

            Log.info("Unbanned.");
        });
    }
}
