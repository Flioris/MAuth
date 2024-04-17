/**
 * Copyright © 2024 Flioris
 *
 * This file is part of MAuth.
 *
 * MAuth is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package flioris.mauth;

import flioris.mauth.util.ConfigHandler;
import lombok.Getter;
import flioris.mauth.db.Core;
import flioris.mauth.listener.BlockedUsersListener;
import flioris.mauth.listener.PlayerListener;
import net.dv8tion.jda.api.JDA;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;

public final class MAuth extends JavaPlugin {
    @Getter
    private static MAuth plugin;
    @Getter
    private static Core core;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        plugin = this;
        core = new Core();
        String token = ConfigHandler.getString("discord.bot-token");

        checkValidity(token);
        registerEvents();
        registerCommands();

        Bot.startBot(token);
    }

    @Override
    public void onDisable() {
        JDA jda = Bot.getJda();

        if (core != null) {
            core.close();
        }
        if (jda == null) {
            return;
        }

        jda.shutdownNow();
        try {
            while (jda.getStatus() != JDA.Status.SHUTDOWN) Thread.sleep(20);
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void checkValidity(String token) {
        HashSet<String> nullValues = new HashSet<>();

        if (token.isEmpty()) {
            nullValues.add("discord.bot-token");
        }
        if (ConfigHandler.getString("discord.guild-id").isEmpty()) {
            nullValues.add("discord.guild-id");
        }
        if (ConfigHandler.getString("discord.emoji").isEmpty()) {
            nullValues.add("discord.emoji");
        }
        if (ConfigHandler.getString("discord.verified-role-id").isEmpty() &&
                ConfigHandler.getBoolean("discord.issuing-verified-role")) {
            nullValues.add("discord.verified-role-id");
        }

        if (!nullValues.isEmpty()) {
            getLogger().severe("Please set the values in the configuration for: " + String.join(", ", nullValues));
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new BlockedUsersListener(), this);
        pluginManager.registerEvents(new PlayerListener(), this);
    }

    private void registerCommands() {
        PluginCommand mainCommand = getCommand("mauth");

        mainCommand.setExecutor(new Commands());
        mainCommand.setTabCompleter(new Commands());
    }
}