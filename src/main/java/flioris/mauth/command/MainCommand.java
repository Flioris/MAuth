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

package flioris.mauth.command;

import flioris.mauth.Bot;
import flioris.mauth.MAuth;
import flioris.mauth.db.Core;
import flioris.mauth.util.ConfigHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {
    private static final Plugin plugin = MAuth.getPlugin();
    private static final Core core = MAuth.getCore();

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
        } else if (args[0].equalsIgnoreCase("reload")) {
            reload(sender);
        } else if (args[0].equalsIgnoreCase("del")) {
            del(sender);
        } else if (args.length == 1) {
            sendHelpMessage(sender);
        } else if (args[0].equalsIgnoreCase("set")) {
            set(sender, args);
        } else if (args[0].equalsIgnoreCase("get")) {
            get(sender, args);
        } else if (args[0].equalsIgnoreCase("getById")) {
            getById(sender, args);
        } else if (args[0].equalsIgnoreCase("addwl")) {
            addwl(sender, args);
        } else if (args[0].equalsIgnoreCase("remwl")) {
            remwl(sender, args);
        } else {
            sendHelpMessage(sender);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> strings = new ArrayList<>();

        if (args.length == 1) {
            strings.add("set");
            strings.add("del");
            strings.add("get");
            strings.add("getById");
            if (sender.hasPermission("mauth.addwl")) {
                strings.add("addwl");
            }
            if (sender.hasPermission("mauth.remwl")) {
                strings.add("remwl");
            }
        } else if (args.length == 2) {
            switch (args[0]) {
                case "set", "getById" -> strings.add("id");
                case "addwl", "remwl" -> strings.add("ip");
                case "get" -> plugin.getServer().getOnlinePlayers().forEach(player -> strings.add(player.getName()));
            }
        }

        return strings;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ConfigHandler.improve("messages.help"));
    }

    private void reload(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage(ConfigHandler.improve("messages.reloaded"));
    }

    private void del(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigHandler.improve("messages.command-for-player"));
            return;
        }

        String account = core.getAccount(player.getName());

        if (account != null) {
            Bot.sendConfirmUnlink(account, player);
        } else {
            player.sendMessage(ConfigHandler.improve("messages.account-not-linked"));
        }
    }

    private void set(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ConfigHandler.improve("messages.command-for-player"));
            return;
        }

        String id = args[1];

        try {
            Long.parseLong(id);
        } catch (NumberFormatException e) {
            id = Bot.getIdByNameId(id);
        }

        if (id == null) {
            player.sendMessage(ConfigHandler.improve("messages.invalid-account"));
            return;
        }

        if (core.getAccount(player.getName()) == null) {
            if (core.getAccountByID(id) != null) {
                player.sendMessage(ConfigHandler.improve("messages.account-busy"));
            } else if (!Bot.hasWhitelistedRole(id)) {
                player.sendMessage(ConfigHandler.improve("messages.not-whitelisted-role"));
            } else {
                Bot.sendConfirmAccount(id, player);
            }
        } else {
            player.sendMessage(ConfigHandler.improve("messages.account-already-linked"));
        }
    }

    private void get(CommandSender sender, String[] args) {
        String account = core.getAccount(args[1]);

        if (account != null) {
            Bot.getJda().retrieveUserById(account).queue(user ->
                    sender.sendMessage(ConfigHandler.improve("messages.player-account")
                            .replace("{player}", args[1])
                            .replace("{id}", user.getName() + " (" + account + ")")));
        } else {
            sender.sendMessage(ConfigHandler.improve("messages.player-account-not-linked"));
        }
    }

    private void getById(CommandSender sender, String[] args) {
        String account = core.getAccountByID(args[1]);

        if (account != null) {
            sender.sendMessage(ConfigHandler.improve("messages.player-account-by-id")
                    .replace("{player}", account)
                    .replace("{id}", args[1]));
        } else {
            sender.sendMessage(ConfigHandler.improve("messages.player-account-not-linked-by-id"));
        }
    }

    private void addwl(CommandSender sender, String[] args) {
        if (sender.hasPermission("mauth.whitelist")) {
            core.addInWhiteList(args[1]);
            sender.sendMessage(ConfigHandler.improve("messages.whitelisted").replace("{ip}", args[1]));
        } else {
            sender.sendMessage(ConfigHandler.improve("messages.no-permission"));
        }
    }

    private void remwl(CommandSender sender, String[] args) {
        if (sender.hasPermission("mauth.whitelist")) {
            core.remFromWhiteList(args[1]);
            sender.sendMessage(ConfigHandler.improve("messages.unwhitelisted").replace("{ip}", args[1]));
        } else {
            sender.sendMessage(ConfigHandler.improve("messages.no-permission"));
        }
    }
}
