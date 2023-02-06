package org.xcore.plugin.modules;

import arc.Events;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.game.*;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.gen.Unitc;
import mindustry.maps.MapException;
import mindustry.net.WorldReloader;
import org.xcore.plugin.XcorePlugin;
import org.xcore.plugin.listeners.NetEvents;

import static mindustry.Vars.world;
import static org.xcore.plugin.PluginVars.config;
import static org.xcore.plugin.Utils.showLeaderboard;

public class MiniHexed {
    public static final ObjectMap<String, Team> teams = new ObjectMap<>();
    public static final ObjectMap<String, Timer.Task> left = new ObjectMap<>();
    private static int winScore = 1800;
    private static Schematic startBase;

    public static void init() {
        if (!config.isMiniHexed()) return;
        Database.init();

        showLeaderboard();
        Vars.netServer.chatFormatter = NetEvents::chat;

        startBase = Schematics.readBase64("bXNjaAF4nDWQ3W6DMAxGv/wQUpDWV+gLcLPXmXaRQap2YhgFurYvv82ONSLlJLGPbYEWvYNf0lfGy0glny75cdr2VHb0U97Gcl33Ky0Awpw+8rzBvr336Eda11yGe5pndCvd+bzQlBFHWr7zkwqOZypjHtZCn3nc+cFNN0K/0ZzKsKYlsygdh+2SyoR4W2ZKUy7o07UM5yTOE8d72rl2fuylvsBPxDvwivpZ2QyvejZCFy387w+/NUbCXrMaRVCvVSUqDopOICfrOJcXV1TdqG5E94wWrmGwLjio1/0PZAMcC6blG2d6RhTBaqbVTCeZkctFA23rNOAlcKh9uIQXs8a9huVmPcPBWYaXORteFUEmaDQzaJfAcoVVVC+oF9QL6gX5Lx0jdppa5w1S7Q8n5z8n");
        Events.on(EventType.PlayEvent.class, event -> applyRules());
        Events.on(EventType.PlayerConnectionConfirmed.class, event -> {
            Database.cachedPlayerData.put(event.player.uuid(), Database.getPlayerData(event.player)
                    .setNickname(event.player.coloredName()));
            initPlayer(event.player);
        });
        Events.on(EventType.PlayerLeave.class, event -> {
            Database.cachedPlayerData.remove(event.player.uuid());
            left.put(event.player.uuid(), Timer.schedule(() -> {
                killTeam(event.player.team());
                teams.remove(event.player.uuid());
                var task = left.remove(event.player.uuid());

                if (task != null) task.cancel();
            }, 120f));
        });
        Events.run(EventType.Trigger.update, () -> teams.each((uuid, team) -> {
            if (team == null) return;

            if (team.cores().size >= 61) {
                endGame();
            }
        }));
        Timer.schedule(() -> {
            if (!Groups.player.isEmpty()) {
                winScore -= 1;
            }
            int sec = winScore % 60;
            int min = (winScore / 60) % 60;

            Groups.player.each(p -> Call.infoPopup(p.con(), Strings.format("[blue]@:@[] until endgame", min, sec),
                    1, Align.bottom, 0, 0, 0, 0));

            if (winScore < 1) {
                endGame();
            }
        }, 0f, 1);

        XcorePlugin.info("MiniHexed loaded.");
    }

    private static void applyRules() {
        UnitTypes.risso.flying = true;
        UnitTypes.minke.flying = true;
        UnitTypes.bryde.flying = true;
        UnitTypes.sei.flying = true;
        UnitTypes.omura.flying = true;
        UnitTypes.retusa.flying = true;
        UnitTypes.oxynoe.flying = true;
        UnitTypes.cyerce.flying = true;
        UnitTypes.aegires.flying = true;
        UnitTypes.navanax.flying = true;

        Vars.state.rules.canGameOver = false;
        Vars.state.rules.waves = false;
        Vars.state.rules.attackMode = false;
        Vars.state.rules.defaultTeam = Team.derelict;

        for (var team : Team.all) {
            team.rules().rtsAi = true;
            team.rules().aiCoreSpawn = false;
        }
    }

    private static void endGame() {
        winScore = 1800;

        var teams = Vars.state.teams.getActive().copy().filter(t -> !t.players.isEmpty()).sort(t -> t.cores.size).reverse();
        teams.truncate(3);

        var builder = new StringBuilder();
        if (!teams.isEmpty()) {
            builder.append("GameOver. Winners:").append("\n");
            for (int i = 0; i < teams.size; i++) {
                var team = teams.get(i);
                var player = team.players.first();

                if (i == 0) {
                    var data = Database.cachedPlayerData.get(player.uuid());
                    data.rating += 1;
                    Database.setPlayerData(data);
                    Database.cachedPlayerData.put(player.uuid(), data);
                }

                builder.append("[orange]").append(i + 1).append(". ")
                        .append(player.coloredName()).append("[][accent]: [cyan]")
                        .append(team.cores.size).append("\n");
            }
        } else {
            builder.append("GameOver. Unfortunately, I couldn't find the winning players.");
        }

        Call.infoMessage(builder.toString());

        reloadMap();
    }

    private static void reloadMap() {
        try {
            var map = Vars.maps.getNextMap(Gamemode.survival, Vars.state.map);
            var reloader = new WorldReloader();
            reloader.begin();

            world.loadMap(map, map.applyRules(Vars.state.rules.mode()));
            Vars.state.rules = Vars.state.map.applyRules(Vars.state.rules.mode());
            applyRules();
            Vars.logic.play();
            teams.clear();
            left.each((uuid, task) -> task.cancel());
            left.clear();
            reloader.end();
        } catch (MapException e) {
            Log.err("@: @", e.map.name(), e.getMessage());
        }
    }

    private static void notAvailableTeamMessage(Player player) {
        player.sendMessage("All cores are busy. You are an observer of the game.");
    }

    private static void initPlayer(Player player) {
        var leftPlayer = left.remove(player.uuid());

        if (leftPlayer != null) {
            leftPlayer.cancel();
        }

        var playerTeam = teams.get(player.uuid());

        if (playerTeam != null && playerTeam.active()) {
            player.team(playerTeam);
            return;
        }

        var core = Team.green.cores().random();
        var team = Seq.select(Team.all, t -> t.id > 5 && !t.active() && t.data().players.isEmpty()).random();

        if (team == null || core == null) {
            notAvailableTeamMessage(player);
            return;
        }

        teams.put(player.uuid(), team);

        core.tile.setNet(Blocks.coreShard, team, 0);

        int x = core.tileX() - startBase.width / 2, y = core.tileY() - startBase.height / 2;

        startBase.tiles.each(st -> {
            var tile = world.tile(st.x + x, st.y + y);
            if (tile == null) return;

            tile.setNet(st.block, team, st.rotation);
            tile.build.configureAny(st.config);
        });

        player.team(team);
    }

    private static void killTeam(Team team) {
        if (team == Team.derelict || !team.data().active()) return;

        team.data().cores.each(core -> core.tile.setNet(Blocks.coreShard, Team.green, 0));

        team.data().destroyToDerelict();

        team.data().units.each(Unitc::kill);
        team.data().plans.clear();
    }
}
