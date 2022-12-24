package org.xcore.plugin.discord;

import arc.util.Strings;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.xcore.plugin.XcorePlugin;
import org.xcore.plugin.comp.Config;

import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MEMBERS;
import static net.dv8tion.jda.api.requests.GatewayIntent.MESSAGE_CONTENT;
import static org.xcore.plugin.PluginVars.*;

public class Bot {
    public static JDA jda;
    public static Role adminRole;
    public static TextChannel mainChannel;
    public static void connect() {
        try {
            jda = JDABuilder.createLight(config.discordBotToken)
                    .enableIntents(GUILD_MEMBERS, MESSAGE_CONTENT)
                    .build()
                    .awaitReady();

            adminRole = jda.getRoleById(config.discordAdminRoleId);

            mainChannel = jda.getTextChannelById(config.discordMainChannelId);
        } catch (Exception e) {
            XcorePlugin.err("Error while connecting to discord: ");
            e.printStackTrace();
        }


    }
}
