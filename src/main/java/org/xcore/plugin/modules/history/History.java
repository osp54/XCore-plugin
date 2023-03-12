package org.xcore.plugin.modules.history;

import arc.struct.Queue;
import mindustry.world.Tile;

import static mindustry.Vars.*;
import static org.xcore.plugin.PluginVars.config;
import static org.xcore.plugin.PluginVars.maxHistoryCapacity;

public class History {

    public static HistoryStack[] history;

    public static boolean enabled() {
        return !config.isMiniHexed();
    }

    public static void clear() {
        history = new HistoryStack[world.width() * world.height()];
    }

    public static void put(HistoryEntry entry, Tile tile) {
        if (tile == emptyTile) return;

        tile.getLinkedTiles(other -> {
            var stack = get(other.array());
            if (stack == null) return;

            stack.add(entry);
        });
    }

    public static HistoryStack get(int index) {
        if (index < 0 || index >= history.length) return null;

        var stack = history[index];
        if (stack == null) history[index] = stack = new HistoryStack();
        return stack;
    }

    public static class HistoryStack extends Queue<HistoryEntry> {
        public HistoryStack() {
            super(maxHistoryCapacity);
        }

        @Override
        public void add(HistoryEntry entry) {
            super.add(entry);
            if (size > maxHistoryCapacity) removeFirst();
        }
    }
}