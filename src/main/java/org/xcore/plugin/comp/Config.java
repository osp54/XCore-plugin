package org.xcore.plugin.comp;

import net.dv8tion.jda.api.entities.channel.Channel;
import org.xcore.plugin.XcorePlugin;

import static org.xcore.plugin.PluginVars.*;

public class Config {
    public String server = "server";
    public boolean consoleEnabled = true;
    public String discordBotToken = "token here";
    public long discordGuildId = 0L;
    public long discordAdminRoleId = 0L;

    public static void load() {
        if (configFile.exists()) {
            config = gson.fromJson(configFile.reader(), Config.class);
            XcorePlugin.info("Config loaded.");
        } else {
            configFile.writeString(gson.toJson(config = new Config()));
            XcorePlugin.info("Config generated.");
        }
    }
}
