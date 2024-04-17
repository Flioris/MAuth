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

package flioris.mauth.listener;

import flioris.mauth.Bot;
import flioris.mauth.MAuth;
import flioris.mauth.db.Core;
import flioris.mauth.event.SuccessAuthEvent;
import flioris.mauth.cache.BlockedUsersCache;
import flioris.mauth.util.ConfigHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    private final Core core = MAuth.getCore();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinEvent(PlayerJoinEvent e){
        Player player = e.getPlayer();
        String playerName = player.getName();
        String playerIp = player.getAddress().getHostString();
        String account = core.getAccount(playerName);

        if (account != null) {
            String session = core.getSession(playerName);
            if (session == null) {
                BlockedUsersCache.add(playerName);
                Bot.sendConfirmJoin(account, player);
                player.sendTitle(
                        ConfigHandler.improve("titles.confirm-join-title"),
                        ConfigHandler.improve("titles.confirm-join-subtitle"),
                        50, 100, 50);
            } else if (!session.equals(playerIp)) {
                BlockedUsersCache.add(playerName);
                core.remSession(playerName);
                Bot.sendConfirmJoin(account, player);
                player.sendTitle(
                        ConfigHandler.improve("titles.confirm-join-title"),
                        ConfigHandler.improve("titles.confirm-join-subtitle"),
                        50, 100, 50);
            } else {
                Bukkit.getPluginManager().callEvent(new SuccessAuthEvent(player));
            }
        } else {
            if (core.getSessionByIP(playerIp) == null || core.isWhiteListed(playerIp)) {
                if (ConfigHandler.getBoolean("spawn.enabled")) {
                    player.teleport(ConfigHandler.getLocation("spawn"));
                }
                BlockedUsersCache.add(playerName);
                player.sendMessage(ConfigHandler.improve("messages.account-not-linked"));
                player.sendTitle(
                        ConfigHandler.improve("titles.account-not-linked-title"),
                        ConfigHandler.improve("titles.account-not-linked-subtitle"),
                        50, 100, 50);
            } else {
                player.kickPlayer(ConfigHandler.improve("messages.account-limit-per-ip"));
            }
        }
    }
}