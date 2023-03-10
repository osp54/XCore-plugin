package org.xcore.plugin.utils.models;

import arc.struct.Seq;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.gen.Unit;
import mindustry.world.blocks.storage.CoreBlock;
import org.xcore.plugin.utils.ai.AttackAi;
import org.xcore.plugin.utils.Utils;

import static mindustry.Vars.world;
import static org.xcore.plugin.modules.hexed.MiniHexed.*;

public class HexMember {
    public String uuid;
    public Team team;
    public Timer.Task left;

    public Utils.UnitState state = Utils.UnitState.IDLE;

    public HexMember(String uuid) {
        this.uuid = uuid;
    }

    public int controlled() {
        if (team == null) return 0;

        return team.data().cores.size;
    }

    public void setUnitState(Utils.UnitState state) {
        if (team != null) {
            this.state = state;
            team.data().units.each(this::handleUnit);
        }
    }

    public void handleUnit(Unit unit) {
        if (unit == null || unit.team != team || unit.type == UnitTypes.mono || unit.type == UnitTypes.poly || unit.type == UnitTypes.mega || unit.isPlayer())
            return;
        if (state == Utils.UnitState.ATTACK) unit.controller(new AttackAi());
        else unit.controller(unit.type.createController(unit));
    }

    public Team join() {
        if (left != null) left.cancel();
        left = null;

        if (team != null && team.active()) {
            return team;
        }

        var team = Seq.select(Team.all, t -> t.id > 5 && !t.active() && t.data().players.isEmpty()).random();
        var spawnCore = getSpawnCore();

        if (team == null || spawnCore == null) {
            return Team.derelict;
        }

        int x = spawnCore.tileX() - startBase.width / 2, y = spawnCore.tileY() - startBase.height / 2;

        startBase.tiles.each(st -> {
            var tile = world.tile(st.x + x, st.y + y);
            if (tile == null) return;

            tile.setNet(st.block, team, st.rotation);
            tile.build.configureAny(st.config);
        });

        this.team = team;
        return team;
    }

    private CoreBlock.CoreBuild getSpawnCore() {
        var allCores = Vars.state.teams.getActive()
                .select(team -> team.team != Team.green)
                .flatMap(team -> team.cores);

        if (allCores.isEmpty())
            return Team.green.cores().random();

        return Team.green.cores().copy().sort(core -> -core.dst(allCores.min(other -> core.dst(other)))).firstOpt();
    }

    public void leave() {
        left = Timer.schedule(() -> {
            if (team != null) killTeam(team);
            members.remove(uuid);
        }, 120f);
    }

    public void cancelTasks() {
        if (left != null) left.cancel();
        left = null;
    }
}
